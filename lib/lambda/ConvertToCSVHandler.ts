import { S3Handler } from "aws-lambda";
import * as XRay from "aws-xray-sdk";
import * as SDK from "aws-sdk";

const AWS = XRay.captureAWS(SDK);

export const handler: S3Handler = async (event, _, callback) => {
  console.log(JSON.stringify(event))

  const clusterName = process.env.CLUSTER_NAME || "";
  const taskDefinitionArn = process.env.TASK_DEF_ARN || "";
  const containerName = process.env.CONTAINER_NAME || "";
  const subnetId = process.env.VPC_SUBNET || "";

  const ecs = new AWS.ECS();

  try {
    await ecs.runTask({
      cluster: clusterName,
      taskDefinition: taskDefinitionArn,
      launchType: "FARGATE",
      networkConfiguration: {
        awsvpcConfiguration: {
          subnets: [subnetId],
          assignPublicIp: "ENABLED",
        },
      },
      overrides: {
        containerOverrides: [
          {
            environment: [
              {
                name: "KEY_NAME",
                value: event.Records[0].s3.object.key,
              }
            ],
            command: ["java", "-jar", "./build/libs/xml-to-csv-converter-1.0-SNAPSHOT.jar"],
            name: containerName,
          }
        ],

      }
    }).promise();

    callback();

  } catch (e) {
    callback(e);

  }
}
