import { dirname, resolve } from "path";
import { fileURLToPath } from "url";
import express from "express";
import bodyParser from "body-parser";
import { getHosts } from "../store/store.js";
import cors from "cors";

const __dirname = dirname(fileURLToPath(import.meta.url));

const staticServer = express();

if (process.env.PROFILE === "Dev") {
  staticServer.use(cors());
}

staticServer.get("/api/hosts", async (_req, res) => {
  res.send(await getHosts());
});

staticServer.use(express.static(resolve(__dirname, "../../public")));
staticServer.get("*", (_req, res) => {
  res.sendFile(resolve(__dirname, "../../public/index.html"));
});
staticServer.use(bodyParser.json());

export default staticServer;
