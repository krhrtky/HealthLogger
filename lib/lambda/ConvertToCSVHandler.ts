import { S3Handler } from "aws-lambda";
import { ECS } from "aws-sdk";

export const handler: S3Handler = async (event, _, callback) => {
  console.log(JSON.stringify(event))

  const clusterName = process.env.CLUSTER_NAME || "";
  const taskDefinitionArn = process.env.TASK_DEF_ARN || "";
  const containerName = process.env.CONTAINER_NAME || "";
  const subnetId = process.env.VPC_SUBNET || "";

  const ecs = new ECS();

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
            command: ["java", "jar", "./build/libs/xmlparser-1.0-SNAPSHOT.jar"],
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
