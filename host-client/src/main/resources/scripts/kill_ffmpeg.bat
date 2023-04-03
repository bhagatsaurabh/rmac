@echo off
PING 1.1.1.1 -n 1 -w 3000 >nul
taskkill /f /im ffmpeg.exe
PING 1.1.1.1 -n 1 -w 1000 >nul
