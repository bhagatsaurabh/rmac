# RMAC API Server

> :information_source: If you don't know what *RMAC* is, please read [this](https://github.com/saurabh-prosoft/rmac#readme) first !

:page_facing_up: [API Specification](https://github.com/saurabh-prosoft/rmac/blob/main/api-server/functions/scripts/swagger.yml)

<br />

The RMAC API Server mainly provides three functionalities for the RMAC Host-Client:

## Registration

Whenever the RMAC Host-Client boots up, the first thing it does is register itself with the RMAC API Server using the unique `ClientName` field configured in `config.rmac`, the server stores this information in the database (Firebase realtime database) and returns a unique `Id` back to the host-client which it stores in its `config.rmac` for future API calls.

If the RMAC Host-Client is already registered using a `ClientName`, calling the registration API with that same `ClientName` will return back the same `Id`.

## Control Commands Buffer

The RMAC API Server provides endpoints to issue control commands to host-clients, since host-clients can be offline the API Server will store the issued commands to the database.

These are the control commands that can be issued:

- _compromised_ - Uninstall RMAC Host-Client from the target host and remove any associated files
- _fetch_ - Upload any file on the host machine to the configured MEGA account
- _system shutdown_ - Shutdown RMAC Host-Client on the target host
- _cam_ - Capture a snapshot from the default camera and upload to the configured MEGA account
- _config_ - Set `config.rmac` configurations

These commands can be issued irrespective of whether the RMAC Host-Client is currently online or offline.

The RMAC Host-Client polls the API Server regularly to check whether any commands have been issued and executes them, hence the RMAC API Server acts as a logical buffer for passive command execution.

## Update Check

The RMAC Host-Client boot process includes a step to call the API Server by passing its own version and check for any update.

The API Server checks the database to get the latest available version of RMAC Host-Client and compares it with the provided version in the API call, if the version sent by RMAC Host-Client is old, it returns a download link (valid for 30 minutes) to the new client jar (stored in the Firebase storage) along with a checksum value.

The RMAC Host-Client will update itself using the download link to a new version and will restart.

<br/>

### Architecture

The RMAC API Server is a collection of two Firebase cloud functions, one acting as the actual API and another used as a trigger function whenever a new version of RMAC Host-Client jar is uploaded to the Firebase Storage.

### The API Function

Provides the three functionalities listed above: RMAC Host-Client Registration, acting as a control commands buffer and serving update check requests.

### The Update Trigger Function

The hosts database (Firebase realtime database) contains two top-level keys `latestHostVersion` and `latestHostChecksum`, signifying the latest available RMAC Host-Client version.

Whenever a new RMAC Host-Client jar file is uploaded to the storage (Firebase Storage) this function gets triggered, it unwraps the jar, reads its version from manifest and updates the `latestHostVersion` key in database, calculates the SHA-256 checksum and updates the value in `latestHostChecksum` key.
