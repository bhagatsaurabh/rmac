# RMAC Bridge Server

> :information_source: If you don't know what *RMAC* is, please read [this](https://github.com/saurabh-prosoft/rmac#readme) first !

The RMAC Bridge Server acts as an interface for communication between the RMAC Host-Clients and RMAC Consoles.

### Why can't we connect Host-Clients and Consoles directly ?

To enable two-way, real-time socket communication between two entities, one of the entity should act as a Socket Server

Neither RMAC Host-Client nor RMAC Console can be a Socket Server because the Host-Client is the spy agent running on host machine and Console is a Web application which can only open socket client connections.

RMAC Bridge Server acts as the Socket Server and lets them connect as Socket Clients, then proxies, transforms and processes messages between them.

<p align="center">
<img src="https://raw.githubusercontent.com/saurabh-prosoft/saurabh-prosoft.github.io/readme-resources/resource/rmac-bridge-server.png" />
</p>

## Comms Specification

There are 8 types of messages that RMAC Host-Client and RMAC Console communicate with:

### `identity`

Whenever a Host-Client or Console opens a client socket connection to the Bridging server, the first message they send is the `identity` message, so that the Bridge Server can recognize whether the new connection is a `host` connection (coming from RMAC Host-Client) or a `console` connection (coming from RMAC Console)

The Bridge Server saves this information within the socket connection reference, so that when consecutive messages starts coming-in, Bridge Server will know who is the sender.

The data within this message depends on the sender, if the sender is a RMAC Host-Client, it will send its unique `Id` received while [registering](https://github.com/saurabh-prosoft/rmac/tree/main/api-server#registration) with RMAC API Server and all of its configuration from `config.rmac`.

If the RMAC Host-Client is not registered yet, it will not have any `Id`, and hence will send a blank field, the Bridge Server will assign a temporary [uuid](https://en.wikipedia.org/wiki/Universally_unique_identifier) to its socket connection.

When RMAC Console is the sender of `identity` message, it will not send any data, the Bridge Server will always assign a new uuid to a new RMAC Console socket connection.

<p align="center">
<img src="https://raw.githubusercontent.com/saurabh-prosoft/saurabh-prosoft.github.io/readme-resources/resource/rmac-bridge-server-identity.png" />
</p>

### `config`

The RMAC Host-Client sends this message along with the entire `config.rmac` data whenever its config gets changed

Upon receiving this message, the Bridge Server broadcasts the same `config` message to all the connected RMAC Consoles

### `hostid`

A non-registered RMAC Host-Client sends this message when it gets registered and receives its unique `Id` from the RMAC API Server, the Bridge Server updates this host's temporary uuid with the registered `Id`, and broadcasts the same message with updated `Id` to all the connected RMAC Consoles

### `terminal:open`, `terminal:data`, `terminal:resize`, `terminal:close`

All these four messages facilitate the remote interactive powershell command-line running on the host machine to be controlled by the RMAC Console online.

The `terminal:open` message is initiated by the RMAC Console, when the end-user tries to open a new terminal connection using the host dashboard.

The `terminal:data` is a bi-directional message signifying input/output.

The `terminal:close` message can be initiated by either Host-Client or Console.

The `terminal:resize` message gets initiated by the RMAC Console when the dimensions of the online terminal gets changed and requires re-sync.

### `command`

This message is sent by RMAC Console when the end-user issues a [control command](https://github.com/saurabh-prosoft/rmac/tree/main/api-server#control-commands-buffer) to be executed on the host machine running RMAC Host-Client

## API

The RMAC Bridge Server also provides 4 API endpoints to be used by RMAC Console:

### GET /api/hosts

Retrieve the list of hosts with their online/offline and registered/un-registered statuses

### GET /api/hosts/{id}/config

Retrieve `config.rmac` configurations of a host with its `id`

### POST /api/hosts/{id}/property

Update configuration of a RMAC Host-Client with `id`

JSON Body:

```json
{
  "name": "string"
  "value": "string"
}
```

### POST /api/hosts/:id/command

Issue a control command to be executed by the RMAC Host-Client with `id`

JSON Body:

```json
{
  "command": "string"
}
```

<br/>

> Note: The `id` is the id stored within RMAC Bridge Server that corresponds to a RMAC Host-Client, this can be a registered `Id` received from RMAC API Server as part of Host-Client registration or a temporary uuid assigned by the Bridge Server
