import * as admin from "firebase-admin";
admin.initializeApp({
  projectId: "test-project",
});

import firebaseTest from "firebase-functions-test";
const test = firebaseTest();
test.mockConfig({
  bucket: {
    name: "test-bucket",
  },
  topic: {
    name: "test-topic",
  },
  slack: {
    token: "test-token",
  },
});

import { putObjectTrigger } from "./index";

describe("putObjectTrigger", () => {
  describe("file name is empty.", () => {
    it("do not anything.", async () => {
      const wrapped = test.wrap(putObjectTrigger);
      const result = await wrapped({});
      expect(result).toBeUndefined();
    });
  });
});
