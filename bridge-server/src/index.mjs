import * as dotenv from "dotenv";
import { setup } from "./config/firebase.mjs";
dotenv.config({ path: `../.env.${process.env.NODE_ENV}` });

import server from "./servers/socket.mjs";

await setup();

const listener = server.listen(process.env.PORT || 80, () => {
  console.log(`Bridge Server started on ${listener.address().port}`);
});
