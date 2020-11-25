import { PassThrough } from "stream";
import { SQSHandler } from "aws-lambda";
import { WebClient } from "@slack/web-api";
import * as XRay from "aws-xray-sdk";
import * as SDK from "aws-sdk";
import { WebAPICallResult } from "@slack/web-api/dist/WebClient";
import { AxiosResponse, default as axios } from "axios";
import type { S3 } from "aws-sdk";

type FileAPICallResult = {
  files: Array<{
    id: string;
    created: number;
    timestamp: number;
    name: string;
    title: string;
    mimetype: string;
    filetype: string;
    pretty_type: string,
    user: string;
    editable: boolean;
    size: number;
    mode: string
    is_external: boolean;
    external_type: string;
    is_public: boolean;
    public_url_shared: boolean;
    display_as_bot: boolean;
    username: string;
    url_private: string;
    url_private_download: string;
    permalink: string;
    permalink_public: string;
    channels: Array<string>;
    groups: Array<any>;
    ims: Array<any>;
    comments_count: number;
  }>;
} & WebAPICallResult;

type QueueRecord = {
  fileId: string;
}

const downloadFile = async (downloadUrl: string, token: string): Promise<any> => {
  return axios.get(downloadUrl, {
    responseType: 'stream',
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
};

const uploadFromStream = (
  fileResponse: AxiosResponse,
  fileName: string,
  bucket: string,
): { passThrough: PassThrough; promise: Promise<S3.ManagedUpload.SendData> } => {
  const s3 = new AWS.S3();
  const passThrough = new PassThrough();

  console.log(`Upload to S3. bucket name: ${bucket}, file name: ${fileName}`);

  const promise = s3
    .upload({
      Bucket: bucket,
      Key: `master/${fileName}`,
      ContentType: fileResponse.headers['content-type'],
      ContentLength: fileResponse.headers['content-length'],
      Body: passThrough,
    })
    .promise();
  return { passThrough, promise };
};

const AWS = XRay.captureAWS(SDK);

export const handler: SQSHandler = async (event, context, callback) => {
  console.log(JSON.stringify(event.Records, null, 2));

  const slackAuthToken = process.env.SLACK_AUTH_TOKEN ?? "";
  const bucketName = process.env.BUCKET_NAME ?? "";

  const client = new WebClient(slackAuthToken);

  try {

    const result = await client.files.list() as FileAPICallResult;

    console.log(JSON.stringify(result, null, 2));

    for (const record of event.Records) {

      const traceData = XRay.utils.processTraceData(record.attributes.AWSTraceHeader);
      const segment = XRay.getSegment();

      if (segment instanceof XRay.Segment) {
        segment.trace_id = traceData.root;
        segment.parent_id = traceData.parent;
        segment.flush()
      }

      const queue = JSON.parse(record.body) as QueueRecord;

      const maybeFile = result.files.find(file => file.id === queue.fileId);

      if (maybeFile == null) {
        throw new Error(`Not found file id: ${queue.fileId}`);

      } else {
        const responseStream = await downloadFile(maybeFile.url_private_download, slackAuthToken);

        const { passThrough, promise } = uploadFromStream(
          responseStream,
          `${maybeFile.id}.${maybeFile.filetype}`,
          bucketName,
        );

        responseStream.data.pipe(passThrough);

        await promise;
      }
    }
    callback(null);

  } catch (e) {
    console.error(e);
    callback(e);
  }
}
