import { APIGatewayProxyHandlerV2 } from "aws-lambda";
import { SQS } from "aws-sdk";

export const handler: APIGatewayProxyHandlerV2 = async (event) => {
  console.log(JSON.stringify(event.body, null, 2));

  if (event.body == null) {
    console.error("Request has no body.")
    return {
      statusCode: 200,
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(event.body),
    };
  }

  const slackEvent = JSON.parse(event.body);
  const messageBody = JSON.stringify({
    fileId: slackEvent?.event?.file_id,
  })

  const queueUrl = process.env.QUEUE_URL ?? "";

  const sqs = new SQS();

  try {
    await sqs.sendMessage({
      QueueUrl: queueUrl,
      MessageBody: messageBody,
    }).promise();

  } catch (e) {
    console.error(e);
  }

  return {
    statusCode: 200,
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(event.body),
  };
}
