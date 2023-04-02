import { dirname, resolve } from "path";
import { fileURLToPath } from "url";
import express from "express";
import { getHosts, getRegisteredHost, state } from "../store/store.js";
import cors from "cors";
import { emit } from "../events/handlers.js";
import { db } from "../config/firebase.js";

const __dirname = dirname(fileURLToPath(import.meta.url));

const staticServer = express();

if (process.env.PROFILE === "Dev") {
  staticServer.use(cors());
}
staticServer.use(express.json());

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
  const { id } = req.params;
  const data = req.body;
  emit(state.hosts[id], "command", null, null, `config ${data.name} ${data.value}}`);
  res.send();
});
staticServer.post("/api/hosts/:id/command", async (req, res) => {
  const { id } = req.params;
  const { command } = req.body;
  if (!id) {
    return res.status(400).send({ code: "HOST_ID_NOT_PROVIDED", message: "Host Id not provided" });
  }
  if (!command || command === "") {
    return res.status(400).send({ code: "BAD_COMMAND", message: "Bad command" });
  }
  const host = await getRegisteredHost(id);
  if (!host) {
    return res.status(404).send({ code: "HOST_ID_NOT_FOUND", message: "Host not found" });
  }

  let commands = host.commands || [];
  commands = [...commands, command];
  await db.ref(id).child("commands").set(commands);
  return res.status(201).send("Commands updated");
});

staticServer.use(express.static(resolve(__dirname, "../../public")));
staticServer.get("*", (_req, res) => {
  res.sendFile(resolve(__dirname, "../../public/index.html"));
});

export default staticServer;

/*
if (!req.query.id) {
            throw new APIError(errors.CLIENT_ID_NOT_PROVIDED);
        }
        if (!Array.isArray(req.body)) {
            throw new APIError(errors.BAD_COMMANDS_FORMAT);
        }

        const dataSnap = await db.ref(req.query.id).get();
        if (dataSnap.exists()) {
            const commandsSnap = await db.ref(req.query.id).child('commands').get();
            let commands = commandsSnap.exists() ? commandsSnap.val() : [];
            commands = [...commands, ...req.body];
            await db.ref(req.query.id).child('commands').set(commands);
            return res.status(200).send('Commands updated');
        } else {
            throw new APIError(errors.CLIENT_ID_NOT_FOUND);
        }
*/
