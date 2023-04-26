# RMAC Bridge Server

<p align="center">
<a href="https://github.com/saurabh-prosoft/rmac/actions/workflows/build-console-deploy-bridge.yml">
<img alt="GitHub Workflow Status" src="https://img.shields.io/github/actions/workflow/status/saurabh-prosoft/rmac/build-console-deploy-bridge.yml?branch=main&label=Build%20%26%20Deploy&logo=data%3Aimage%2Fpng%3Bbase64%2CiVBORw0KGgoAAAANSUhEUgAAABAAAAAQEAYAAABPYyMiAAAABGdBTUEAALGPC%2FxhBQAAACBjSFJNAAB6JgAAgIQAAPoAAACA6AAAdTAAAOpgAAA6mAAAF3CculE8AAAABmJLR0QAAAAAAAD5Q7t%2FAAAACXBIWXMAAABgAAAAYADwa0LPAAAAB3RJTUUH5gUKFyETutzc4wAABApJREFUSMedlH1M1WUUxz%2Fnd2HGVsyLeMk%2F2HRo%2BgerSeIE3eRNJoo01Li6xaw1UAspV7zNF0Rteq9by%2BlUslUiMzEvKPEiV2DpFJZbOcNcGBW5ZclVzIGIee%2Fv9Ede2rjdXev717Nn55zP95zn2RHGqazMtV21qYk2%2BQjS02nTU9DdbZi%2BPigq2jUlL0%2Fk2jX%2Bo8oW1TtVV63CrfGwezefyh%2BwZ4%2BMD6xIa5ipmpTkazF9cOaMVOKAyEiW4oGBAWOGz4rGx%2B%2BakmcXw%2BMJBnxLG1R14sSnEsxXUZeLTHKQtLSxgNucgP5%2Bo%2FQb14hqc3PFFtf7qvPn7%2BrM7RXp7qaATLSoaCyhmclgs%2Fm%2BCvsFVq8O1XF4%2FqPPwWqlHjuycGFAwA15ESoqDHmJLZCSYj7NILS2lpe74lULC6nmPCxbNj5PjukpJCYmGPjdV%2BqdatpshtNyFRobOcQDsFhok40wOoqbRrSz03F2eanI8eNheFmHdnUxyLdIRobCbKiulipA%2FoUwTXrBag0O1mGko0P28gLEx%2FMsL6OtrUaUzEDy88053iXg9XKZIwCG7pZRpLiYJj6AW7dCfyf9ENavL%2Fv5ZKPq1q2gqipiydSpcPq0H6x2sqClZWRmxPeQm%2Fv3096543Dm2cW4d29sov7DOwlfJKgZHW2Z%2FucpJC9P6ogFm00iCQerVd%2BgDoqLA%2Fys03CorDQ%2Blj7o6PDlMws2bHjQFzEdXbNm35Ilz4nx8GGwdoQnVLn95HU1S0p0qvQgTmfAXEpxwbZtzkkrjolUVT1p3ZAGSnc2rFVz7lzLQ3MmEh6u1zUbTU4OZsQ%2FEce0lTki27f%2FbwMl5%2BsnqSYkGHF6Fc6epVnS0AkTKPBdQ7Kz6TTeRBcswE06smNHQOFGKYf9%2BxnRGXD3rllAJzowYHnddwTq6vx7JMCAv2MZMksRt5tZHISbN3UpI%2BjFi%2FIJUYjdbgwTBVlZZgp9aGpqMCMB%2BmehRUFqqhEK7PUxgmZlSTuVSGwsg7wGpumbYPSijx450lccEGPnTi5wC%2FbtC2ng8UIzEyyz0b17jfIvXTfUTEwMBg4b4hAcPMgVhmHePH3GcKKZmc7NudViXLo0VvgC62FwMAD4q3wGtbXUyVy0sJAoLsPQEFWSA0lJhuYyDDU1lLMFIiMlw5eI5uSMgX%2FnByQ5OSj4sfQ9ItDbtwPuRW2o2%2B3oXx4rxuHD1OgxWLxY5%2BjXyLlzUtbjmqOmx0MtFUh0NHGyGb1%2Fnx%2B1CfF6Q4H9qvjtRJ2akyebGZb9SE8P2bwNMTFjHT8GO75b%2BZNIV5c%2Fz5ArrEWKiuhmEXg83NM2pL9fG3QjmpERCuyX%2F1drqRyA1FRqeB5tbyeOZRAWpkflKmzaND7vL7TQ7bt9WJJoAAAAJXRFWHRkYXRlOmNyZWF0ZQAyMDIyLTA1LTEwVDIzOjMzOjE5KzAwOjAw7KPZpwAAACV0RVh0ZGF0ZTptb2RpZnkAMjAyMi0wNS0xMFQyMzozMzoxOSswMDowMJ3%2BYRsAAAAASUVORK5CYII%3D&style=flat-square">
</a>
</p>

<br/>

> :information_source: If you don't know what _RMAC_ is, please read [this](https://github.com/saurabh-prosoft/rmac#readme) first !

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
