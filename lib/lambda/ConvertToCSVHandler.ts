import { S3Handler } from "aws-lambda";


export const handler: S3Handler = async (event, context, callback) => {
  console.log(JSON.stringify(event))
  console.log(JSON.stringify(context))
  callback()
}