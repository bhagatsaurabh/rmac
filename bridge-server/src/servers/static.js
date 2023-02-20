import { dirname, resolve } from "path";
import { fileURLToPath } from "url";
import express from "express";

const __dirname = dirname(fileURLToPath(import.meta.url));

const staticServer = express();
staticServer.use(express.static(resolve(__dirname, "../../public")));
staticServer.get("*", (_req, res) => {
  res.sendFile(resolve(__dirname, "../../public/index.html"));
});

export default staticServer;
