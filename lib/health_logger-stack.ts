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
    });

    const api = new LambdaRestApi(this, 'FileUploadEventAPI', {
      handler: fileUploadSubscribeHandler,
      proxy: false,
    });

    const file = api.root.addResource('file');
    file.addMethod("POST");

    const bucket = new Bucket(this, "HealthLoggerData", {
      accessControl: BucketAccessControl.PRIVATE,
    });

    new StringParameter(this, "HealthLoggerDataBucketName", {
      parameterName: "HealthLoggerDataBucketName",
      stringValue: bucket.bucketName
    });

    const slackAuthToken = this.node.tryGetContext('slack_auth_token');

    const fileUploadToS3Handler = new NodejsFunction(this, 'FileUploadToS3Handler', {
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
    });

    fileUploadToS3Handler.addEventSource(new SqsEventSource(queue));

    const convertToCSVHandler = new NodejsFunction(this, 'ConvertToCSVHandler', {
      entry: resolve(__dirname, './lambda/ConvertToCSVHandler.ts'),
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

    new cdk.CfnOutput(this, "FileUploadEventSubscribeAPI", {
      value: api.url,
    });
  }
}
