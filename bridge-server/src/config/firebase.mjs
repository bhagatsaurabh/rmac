import { initializeApp } from "firebase-admin/app";
import firebase from "firebase-admin";
import fs from "fs/promises";

let db = null;

const setup = async () => {
  let adminConfig;
  if (process.env.PROFILE === "Dev") {
    const key = await fs.readFile(process.env.FIREBASE_ADMIN_KEY_PATH);
    adminConfig = JSON.parse(key);
  } else if (process.env.PROFILE === "Prod") {
    adminConfig = JSON.parse(
      Buffer.from(process.env.FIREBASE_ADMIN_KEY_BASE64, "base64").toString()
    );
  }

  initializeApp(adminConfig);
  db = firebase.database();
};

export { setup, db };
