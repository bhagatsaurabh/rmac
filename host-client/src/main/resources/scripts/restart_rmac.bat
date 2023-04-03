timeout /t 3
taskkill /f /im java.exe
timeout /t 2
start /B "" "${STARTUP_LOCATION}\rmac.vbs"
