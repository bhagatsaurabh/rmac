import { initializeApp } from "firebase-admin/app";
import firebase from "firebase-admin";
import { pathToFileURL } from "node:url";

let adminConfig;
if (process.env.PROFILE === "Dev") {
  adminConfig = (
    await import(pathToFileURL(process.env.FIREBASE_ADMIN_KEY_PATH), {
      assert: { type: "json" },
    })
  ).default;
} else if (process.env.PROFILE === "Prod") {
  adminConfig = JSON.parse(Buffer.from(process.env.FIREBASE_ADMIN_KEY_BASE64, "base64").toString());
}

initializeApp(adminConfig);
let db = firebase.database();

export { db };
