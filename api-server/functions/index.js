import { getServer } from './api/index.js';
import { updaterFunction } from './updater/updater.js';
import * as context from './config/firebase.js';

export const api = context.https.onRequest(getServer(context));
export const rmacClientUpdater = context.storage.object().onFinalize(updaterFunction);
