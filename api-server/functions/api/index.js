import express from "express";
import bodyParser from "body-parser";

import { errorHandler } from "../middleware/error-handler.js";
import { getRegister } from "./register.js";
import { getCommand, postCommand } from "./command.js";
import { getUpdate } from "./update.js";

const getServer = (context) => {
  const server = express();

  server.use("/docs", express.static("../docs"));

  server.use(bodyParser.json());
  server.use((req, _res, next) => {
    req.context = context;
    next();
  });

  server.get("/api/register", getRegister);
  server.get("/api/command", getCommand);
  server.post("/api/command", postCommand);
  server.get("/api/update", getUpdate);

  server.use(errorHandler);

  return server;
};

export { getServer };
