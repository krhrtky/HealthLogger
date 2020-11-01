import {resolve} from "path";
import * as cdk from '@aws-cdk/core';
import { NodejsFunction } from "@aws-cdk/aws-lambda-nodejs";
import { LambdaRestApi } from "@aws-cdk/aws-apigateway";
import { Queue } from "@aws-cdk/aws-sqs";
import { PolicyStatement } from "@aws-cdk/aws-iam";

export class HealthLoggerStack extends cdk.Stack {
  constructor(scope: cdk.Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    const queue = new Queue(this, "FileCreateQueue");

    const lambda = new NodejsFunction(this, 'LambdaHandler', {
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
      handler: lambda,
      proxy: false,
    });

    const file = api.root.addResource('file');
    file.addMethod("POST");

    new cdk.CfnOutput(this, "FileUploadEventSubscribeAPI", {
      value: api.url,
    });
  }
}
