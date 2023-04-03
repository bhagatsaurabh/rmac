timeout /t 4
taskkill /f /im java.exe
rd /s /q "${JRE_LOCATION}"
rd /s /q "${SCRIPTS_LOCATION}"
rd /s /q "${CONFIG_LOCATION}"
del /f /q "${RUNTIME_LOCATION}\ffmpeg.exe"
del /f /q "${RUNTIME_LOCATION\nircmd.exe"
rd /s /q "${MEGACMD_LOCATION}"
del /f /q "${STARTUP_LOCATION}\rmac.vbs"
rd /q /s "${RUNTIME_LOCATION}\update"
rd /q /s "${CURRENT_LOCATION}"
del "%~f0"
