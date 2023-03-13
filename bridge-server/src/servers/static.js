import { dirname, resolve } from "path";
import { fileURLToPath } from "url";
import express from "express";
import bodyParser from "body-parser";
import { getHosts, state } from "../store/store.js";
import cors from "cors";
import { emit } from "../events/handlers.js";

const __dirname = dirname(fileURLToPath(import.meta.url));

const staticServer = express();

if (process.env.PROFILE === "Dev") {
  staticServer.use(cors());
}

staticServer.get("/api/hosts", async (_req, res) => {
  res.send(await getHosts());
});
staticServer.get("/api/hosts/:id/config", (req, res) => {
  const id = req.params.id;
  if (!state.configs[id]) {
    res.status(204).send();
  } else {
    res.send(state.configs[id]);
  }
});
staticServer.post("/api/hosts/:id/property", (req, res) => {
  const id = req.params.id;
  const data = req.body;
  emit(state.hosts[id], "command", null, null, `prop ${data.name} ${data.value}}`);
  res.send();
});

staticServer.use(express.static(resolve(__dirname, "../../public")));
staticServer.get("*", (_req, res) => {
  res.sendFile(resolve(__dirname, "../../public/index.html"));
});
staticServer.use(bodyParser.json());

export default staticServer;
