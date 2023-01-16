import { initializeApp } from 'firebase-admin/app';
import firebase from 'firebase-admin';
import * as adminStorage from 'firebase-admin/storage';
import { logger, https, storage } from 'firebase-functions';

const adminConfig = JSON.parse(process.env.FIREBASE_CONFIG);
initializeApp(adminConfig);
const db = firebase.database();
const bucket = adminStorage.getStorage().bucket();

export { db, bucket, logger, https, storage };