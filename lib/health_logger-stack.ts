import { resolve } from "path";
import * as cdk from '@aws-cdk/core';
import { Duration } from '@aws-cdk/core';
import { NodejsFunction } from "@aws-cdk/aws-lambda-nodejs";
import { LambdaRestApi } from "@aws-cdk/aws-apigateway";
import { Queue } from "@aws-cdk/aws-sqs";
import { PolicyStatement } from "@aws-cdk/aws-iam";
import { Bucket, BucketAccessControl, EventType } from "@aws-cdk/aws-s3";
import { S3EventSource, SqsEventSource } from "@aws-cdk/aws-lambda-event-sources";
import { StringParameter } from "@aws-cdk/aws-ssm";
import { DockerImageAsset } from "@aws-cdk/aws-ecr-assets";
import { Cluster, ContainerImage, FargateTaskDefinition, LogDriver } from "@aws-cdk/aws-ecs";
import { Vpc } from "@aws-cdk/aws-ec2";
import { Tracing } from "@aws-cdk/aws-lambda";
import { AttributeType, BillingMode, Table } from "@aws-cdk/aws-dynamodb";
import { DynamoDbDataSource, FieldLogLevel, GraphqlApi, MappingTemplate, Schema } from "@aws-cdk/aws-appsync";

export class HealthLoggerStack extends cdk.Stack {
  constructor(scope: cdk.Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    const queue = new Queue(this, "FileCreateQueue", {
      deliveryDelay: Duration.minutes(1),
    });

    const fileUploadSubscribeHandler = new NodejsFunction(this, 'FileUploadSubscribeHandler', {
      entry: resolve(__dirname, './lambda/FileUploadEventHandler.ts'),
      environment: {
        QUEUE_URL: queue.queueUrl,
      },
      initialPolicy: [
        new PolicyStatement({
          actions: ["sqs:SendMessage"],
          resources: [queue.queueArn],
        })
      ],
      tracing: Tracing.ACTIVE,
    });

    const api = new LambdaRestApi(this, 'FileUploadEventAPI', {
      handler: fileUploadSubscribeHandler,
      proxy: false,
    });

    const file = api.root.addResource('file');
    file.addMethod("POST");

    const bucket = new Bucket(this, "HealthLoggerData", {
      accessControl: BucketAccessControl.PRIVATE,
      lifecycleRules: [{ expiration: Duration.days(7)}],
    });

    const dynamoDB = new Table(this, "StepCount", {
      tableName: "StepCount",
      partitionKey: {
        name: "startDate",
        type: AttributeType.NUMBER,
      },
      sortKey: {
        name: "creationDate",
        type: AttributeType.NUMBER,
      },
      billingMode: BillingMode.PAY_PER_REQUEST,
    });

    const ssm = new StringParameter(this, "HealthLoggerDataBucketName", {
      parameterName: "HealthLoggerDataBucketName",
      stringValue: bucket.bucketName
    });

    const slackAuthToken = this.node.tryGetContext('slack_auth_token');

    const fileUploadToS3Handler = new NodejsFunction(this, 'FileUploadToS3Handler', {
      functionName: "FileUploadToS3Handler",
      entry: resolve(__dirname, './lambda/FileUploadToS3Handler.ts'),
      environment: {
        SLACK_AUTH_TOKEN: slackAuthToken,
        BUCKET_NAME: bucket.bucketName,
      },
      initialPolicy: [
        new PolicyStatement({
          actions: ["s3:PutObject"],
          resources: [bucket.bucketArn, `${bucket.bucketArn}/*`],
        })
      ],
      tracing: Tracing.ACTIVE,
    });

    const imageAsset = new DockerImageAsset(this, "XmlHandler", {
      directory: resolve(__dirname, "../xml-handler"),
    });

    /*
     * CSV Converter Task
     */
    const vpc = new Vpc(this, "XmlToCsvConverterVpc", { maxAzs: 1 });
    const cluster = new Cluster(this, "XmlToCsvConverterCluster", {
      vpc,
      containerInsights: true,
    });

    const converterTaskDef = new FargateTaskDefinition(this, "XmlToCsvConverterTask", {
      cpu: 1024,
      memoryLimitMiB: 5120,
    });

    converterTaskDef.addToTaskRolePolicy(
      new PolicyStatement({
        actions: ["s3:PutObject", "s3:GetObject"],
        resources: [bucket.bucketArn, `${bucket.bucketArn}/*`],
      }),
    );

    converterTaskDef.addToTaskRolePolicy(
      new PolicyStatement({
        actions: ["ssm:GetParameter"],
        resources: [ssm.parameterArn],
      }),
    );

    const converterContainer = converterTaskDef.addContainer("XmlToCsvConverterContainer", {
      image: ContainerImage.fromDockerImageAsset(imageAsset),
      logging: LogDriver.awsLogs({ streamPrefix: "XmlToCsvConverter" })
    });

    fileUploadToS3Handler.addEventSource(new SqsEventSource(queue));

    const convertToCSVHandler = new NodejsFunction(this, 'ConvertToCSVHandler', {
      entry: resolve(__dirname, './lambda/ConvertToCSVHandler.ts'),
      initialPolicy: [
        new PolicyStatement({
          actions: ["ecs:RunTask"],
          resources: [converterTaskDef.taskDefinitionArn],
        }),
        new PolicyStatement({
          actions: ["ecs:StopTask"],
          resources: [converterTaskDef.taskDefinitionArn],
        }),
        new PolicyStatement({
          actions: ["iam:PassRole"],
          resources: ["*"],
        }),
      ],
      environment: {
        CLUSTER_NAME: cluster.clusterName,
        TASK_DEF_ARN: converterTaskDef.taskDefinitionArn,
        CONTAINER_NAME: converterContainer.containerName,
        VPC_SUBNET: vpc.publicSubnets[0].subnetId,
      },
      tracing: Tracing.ACTIVE,
    });

    convertToCSVHandler.addEventSource(new S3EventSource(
      bucket,
      {
        events: [EventType.OBJECT_CREATED_PUT],
        filters: [{
          prefix: "master/",
          suffix: "zip",
        }]
      }
    ));

    /*
     * Save to DB Task
     */
    const saveToDBVpc = new Vpc(this, "SaveToDBVpc", { maxAzs: 1 });
    const saveToDBCluster = new Cluster(this, "SaveToDBCluster", {
      vpc: saveToDBVpc,
      containerInsights: true,
    });

    const saveToDBTaskDef = new FargateTaskDefinition(this, "SaveToDBTask", {
      cpu: 1024,
      memoryLimitMiB: 5120,
    });

    saveToDBTaskDef.addToTaskRolePolicy(
      new PolicyStatement({
        actions: ["s3:GetObject"],
        resources: [bucket.bucketArn, `${bucket.bucketArn}/*`],
      }),
    );

    saveToDBTaskDef.addToTaskRolePolicy(
      new PolicyStatement({
        actions: ["ssm:GetParameter"],
        resources: [ssm.parameterArn],
      }),
    );

    saveToDBTaskDef.addToTaskRolePolicy(
      new PolicyStatement({
        actions: ["dynamodb:PutItem", "dynamodb:Scan"],
        resources: [dynamoDB.tableArn],
      }),
    );

    const saveToDBContainer = saveToDBTaskDef.addContainer("SaveToDBContainer", {
      image: ContainerImage.fromDockerImageAsset(imageAsset),
      logging: LogDriver.awsLogs({ streamPrefix: "SaveToDBConverter" })
    });

    const saveToDBHandler = new NodejsFunction(this, 'SaveToDBHandler', {
      entry: resolve(__dirname, './lambda/SaveToDBHandler.ts'),
      initialPolicy: [
        new PolicyStatement({
          actions: ["ecs:RunTask"],
          resources: [saveToDBTaskDef.taskDefinitionArn],
        }),
        new PolicyStatement({
          actions: ["ecs:StopTask"],
          resources: [saveToDBTaskDef.taskDefinitionArn],
        }),
        new PolicyStatement({
          actions: ["iam:PassRole"],
          resources: ["*"],
        }),
      ],
      environment: {
        CLUSTER_NAME: saveToDBCluster.clusterName,
        TASK_DEF_ARN: saveToDBTaskDef.taskDefinitionArn,
        CONTAINER_NAME: saveToDBContainer.containerName,
        VPC_SUBNET: saveToDBVpc.publicSubnets[0].subnetId,
      },
      tracing: Tracing.ACTIVE,
    });

    saveToDBHandler.addEventSource(new S3EventSource(
      bucket,
      {
        events: [EventType.OBJECT_CREATED_PUT],
        filters: [{
          prefix: "convert/stepCount",
        }]
      }
    ));

    const graphqlAPI = new GraphqlApi(this, "RecordAPI", {
      name: "recordAPI",
      schema: Schema.fromAsset(resolve(__dirname, "./GraphQL/schema.graphql")),
      logConfig: {
        fieldLogLevel: FieldLogLevel.ALL,
        excludeVerboseContent: true,
      },
      xrayEnabled: true,
    });

    const appSync = new DynamoDbDataSource(this, "RecordAPIDataSource", {
      table: dynamoDB,
      api: graphqlAPI,
    });

    appSync.createResolver({
      typeName: "Query",
      fieldName: "listStepCounts",
      requestMappingTemplate: MappingTemplate.fromString(`
        {
          "version": "2017-02-28",
          "operation": "Scan",
          "limit": $util.defaultIfNull($ctx.args.limit, 20),
          "nextToken": $util.toJson($util.defaultIfNullOrEmpty($ctx.args.nextToken, null)),
        }
      `),
      responseMappingTemplate: MappingTemplate.dynamoDbResultItem(),
    });

    new cdk.CfnOutput(this, "FileUploadEventSubscribeAPI", {
      value: api.url,
    });
  }
}
