import * as functions from "firebase-functions";
import {PubSub} from "@google-cloud/pubsub";
import {Storage} from "@google-cloud/storage";
// eslint-disable-next-line no-unused-vars
import type {WebAPICallResult} from "@slack/web-api/dist/WebClient";
import {WebClient} from "@slack/web-api";
import {default as axios} from "axios";

/* eslint-disable camelcase */
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
/* eslint-able camelcase */


const config = functions.config();
const bucketName = config.bucket.name as string;
const topicName = config.topic.name as string;
const slackAuthToken = config.slack.token as string;

export const fetchAndSaveLatestHealthLog = functions
    .pubsub
    .schedule("00 00 * * *")
    .timeZone("Asia/Tokyo")
    .onRun(async () => {
      const client = new WebClient(slackAuthToken);
      const result = await client.files.list() as FileAPICallResult;

      const latest = result
          .files
          .reduce((acc, current) => {
            return acc.timestamp > current.timestamp ? acc : current;
          });

      functions
          .logger
          .info(`Latest file: ${latest.name}, timestamp: ${latest.timestamp}`);

      const bucket = new Storage().bucket(bucketName);
      const file = bucket.file(`master/${latest.timestamp}.zip`);
      const exists = (await file.exists())[0];

      if (exists) {
        functions
            .logger
            .info(`Latest file: ${latest.timestamp}.zip already exists.`);
        return;
      }

      await axios.get(latest.url_private_download, {
        responseType: "stream",
        headers: {
          Authorization: `Bearer ${slackAuthToken}`,
        },
      }).then((res) => {
        res.data.pipe(file.createWriteStream());
      });

      return null;
    });

export const putObjectTrigger = functions
    .storage
    .bucket(bucketName)
    .object()
    .onFinalize(async ({name}) => {
      console.log(`File name: ${name}`);

      if (name === undefined) {
        console.error("Finalized file name is empty.");
        return;
      }
      const pubSub = new PubSub();

      if (name?.startsWith("master/")) {
        await pubSub.topic(topicName).publish(Buffer.from(name));
      }
    });
