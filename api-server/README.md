# RMAC API Server

<p align="center">
<a href="https://github.com/bhagatsaurabh/rmac/actions/workflows/build-deploy-api-server.yml">
<img alt="GitHub Workflow Status" src="https://img.shields.io/github/actions/workflow/status/bhagatsaurabh/rmac/build-deploy-api-server.yml?branch=main&label=Build%20%26%20Deploy&logo=data%3Aimage%2Fpng%3Bbase64%2CiVBORw0KGgoAAAANSUhEUgAAABAAAAAQEAYAAABPYyMiAAAABGdBTUEAALGPC%2FxhBQAAACBjSFJNAAB6JgAAgIQAAPoAAACA6AAAdTAAAOpgAAA6mAAAF3CculE8AAAABmJLR0QAAAAAAAD5Q7t%2FAAAACXBIWXMAAABgAAAAYADwa0LPAAAAB3RJTUUH5gUKFyETutzc4wAABApJREFUSMedlH1M1WUUxz%2Fnd2HGVsyLeMk%2F2HRo%2BgerSeIE3eRNJoo01Li6xaw1UAspV7zNF0Rteq9by%2BlUslUiMzEvKPEiV2DpFJZbOcNcGBW5ZclVzIGIee%2Fv9Ede2rjdXev717Nn55zP95zn2RHGqazMtV21qYk2%2BQjS02nTU9DdbZi%2BPigq2jUlL0%2Fk2jX%2Bo8oW1TtVV63CrfGwezefyh%2BwZ4%2BMD6xIa5ipmpTkazF9cOaMVOKAyEiW4oGBAWOGz4rGx%2B%2BakmcXw%2BMJBnxLG1R14sSnEsxXUZeLTHKQtLSxgNucgP5%2Bo%2FQb14hqc3PFFtf7qvPn7%2BrM7RXp7qaATLSoaCyhmclgs%2Fm%2BCvsFVq8O1XF4%2FqPPwWqlHjuycGFAwA15ESoqDHmJLZCSYj7NILS2lpe74lULC6nmPCxbNj5PjukpJCYmGPjdV%2BqdatpshtNyFRobOcQDsFhok40wOoqbRrSz03F2eanI8eNheFmHdnUxyLdIRobCbKiulipA%2FoUwTXrBag0O1mGko0P28gLEx%2FMsL6OtrUaUzEDy88053iXg9XKZIwCG7pZRpLiYJj6AW7dCfyf9ENavL%2Fv5ZKPq1q2gqipiydSpcPq0H6x2sqClZWRmxPeQm%2Fv3096543Dm2cW4d29sov7DOwlfJKgZHW2Z%2FucpJC9P6ogFm00iCQerVd%2BgDoqLA%2Fys03CorDQ%2Blj7o6PDlMws2bHjQFzEdXbNm35Ilz4nx8GGwdoQnVLn95HU1S0p0qvQgTmfAXEpxwbZtzkkrjolUVT1p3ZAGSnc2rFVz7lzLQ3MmEh6u1zUbTU4OZsQ%2FEce0lTki27f%2FbwMl5%2BsnqSYkGHF6Fc6epVnS0AkTKPBdQ7Kz6TTeRBcswE06smNHQOFGKYf9%2BxnRGXD3rllAJzowYHnddwTq6vx7JMCAv2MZMksRt5tZHISbN3UpI%2BjFi%2FIJUYjdbgwTBVlZZgp9aGpqMCMB%2BmehRUFqqhEK7PUxgmZlSTuVSGwsg7wGpumbYPSijx450lccEGPnTi5wC%2FbtC2ng8UIzEyyz0b17jfIvXTfUTEwMBg4b4hAcPMgVhmHePH3GcKKZmc7NudViXLo0VvgC62FwMAD4q3wGtbXUyVy0sJAoLsPQEFWSA0lJhuYyDDU1lLMFIiMlw5eI5uSMgX%2FnByQ5OSj4sfQ9ItDbtwPuRW2o2%2B3oXx4rxuHD1OgxWLxY5%2BjXyLlzUtbjmqOmx0MtFUh0NHGyGb1%2Fnx%2B1CfF6Q4H9qvjtRJ2akyebGZb9SE8P2bwNMTFjHT8GO75b%2BZNIV5c%2Fz5ArrEWKiuhmEXg83NM2pL9fG3QjmpERCuyX%2F1drqRyA1FRqeB5tbyeOZRAWpkflKmzaND7vL7TQ7bt9WJJoAAAAJXRFWHRkYXRlOmNyZWF0ZQAyMDIyLTA1LTEwVDIzOjMzOjE5KzAwOjAw7KPZpwAAACV0RVh0ZGF0ZTptb2RpZnkAMjAyMi0wNS0xMFQyMzozMzoxOSswMDowMJ3%2BYRsAAAAASUVORK5CYII%3D&style=flat-square">
</a>
</p>

<br/>

> :information_source: If you don't know what _RMAC_ is, please read [this](https://github.com/bhagatsaurabh/rmac#readme) first !

:page_facing_up: [API Specification](https://github.com/bhagatsaurabh/rmac/blob/main/api-server/functions/scripts/swagger.yml)

<br />

### The RMAC API Server mainly provides following three functionalities for the RMAC Host-Client:

## Registration

Whenever the RMAC Host-Client boots up, the first thing it does is register itself with the RMAC API Server using the unique `ClientName` field configured in `config.rmac`, the server stores this information in the database (Firebase realtime database) and returns a unique `Id` back to the host-client which it stores in its `config.rmac` for future API calls.

<p align="center">
<img src="https://raw.githubusercontent.com/bhagatsaurabh/bhagatsaurabh.github.io/readme-resources/rmac/rmac-host-client-registration.png" />
</p>

If the RMAC Host-Client is already registered using a `ClientName`, calling the registration API with that same `ClientName` will return back the same `Id`.

## Control Commands Buffer

The RMAC API Server provides endpoints to issue control commands to host-clients, since host-clients can be offline the API Server will store the issued commands to the database.

<p align="center">
<img src="https://raw.githubusercontent.com/bhagatsaurabh/bhagatsaurabh.github.io/readme-resources/rmac/rmac-host-client-command-offline.png" />
</p>

These are the control commands that can be issued:

- _fetch_ - Upload any file on the host machine to the configured MEGA account
- _system shutdown_ - Shutdown RMAC Host-Client on the target host
- _cam_ - Capture a snapshot from the default camera and upload to the configured MEGA account
- _config_ - Set `config.rmac` configurations

These commands can be issued irrespective of whether the RMAC Host-Client is currently online or offline.

The RMAC Host-Client polls the API Server regularly to check whether any commands have been issued and executes them, hence the RMAC API Server acts as a logical buffer for passive command execution.

<p align="center">
<img src="https://raw.githubusercontent.com/bhagatsaurabh/bhagatsaurabh.github.io/readme-resources/rmac/rmac-host-client-command-online.png" />
</p>

## Update Check

The RMAC Host-Client boot process includes a step to call the API Server by passing its own version and check for any update.

The API Server checks the database to get the latest available version of RMAC Host-Client and compares it with the provided version in the API call, if the version sent by RMAC Host-Client is old, it returns a download link (valid for 30 minutes) to the new client jar (stored in the Firebase storage) along with a checksum value.

The RMAC Host-Client will update itself using the download link to a new version and will restart.

<p align="center">
<img src="https://raw.githubusercontent.com/bhagatsaurabh/bhagatsaurabh.github.io/readme-resources/rmac/rmac-host-client-update.png" />
</p>

<br/>

### Architecture

The RMAC API Server is a collection of two Firebase cloud functions, one acting as the actual API and another used as a trigger function whenever a new version of RMAC Host-Client jar is uploaded to the Firebase Storage.

### The API Function

Provides the three functionalities listed above: RMAC Host-Client Registration, acting as a control commands buffer and serving update check requests.

### The Update Trigger Function

The hosts database (Firebase realtime database) contains two top-level keys `latestHostVersion` and `latestHostChecksum`, signifying the latest available RMAC Host-Client version.

Whenever a new RMAC Host-Client jar file is uploaded to the storage (Firebase Storage) this function gets triggered, it unwraps the jar, reads its version from manifest and updates the `latestHostVersion` key in database, calculates the SHA-256 checksum and updates the value in `latestHostChecksum` key.

<p align="center">
<img src="https://raw.githubusercontent.com/bhagatsaurabh/bhagatsaurabh.github.io/readme-resources/rmac/rmac-host-client-update-function.png" />
</p>

> Note: The RMAC Host-Client jar uploaded to Firebase storage must have the same name as its version, for e.g. uploading a new RMAC Host-Client version 2.1.3 should have the name 2.1.3.jar
