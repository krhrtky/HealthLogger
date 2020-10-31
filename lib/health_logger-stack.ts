import {resolve} from "path";
import * as cdk from '@aws-cdk/core';
import { NodejsFunction } from "@aws-cdk/aws-lambda-nodejs";
import { LambdaRestApi } from "@aws-cdk/aws-apigateway";

export class HealthLoggerStack extends cdk.Stack {
  constructor(scope: cdk.Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    const lambda = new NodejsFunction(this, 'LambdaHandler', {
      entry: resolve(__dirname, './lambda/FileUploadEventHandler.ts'),
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
