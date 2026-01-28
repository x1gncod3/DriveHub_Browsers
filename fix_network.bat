@echo off
echo Fixing emulator network settings...

REM Try to find and kill existing emulator
taskkill /f /im emulator.exe 2>nul
timeout /t 3

REM Set environment variables
set ANDROID_SDK_ROOT=C:\Users\IMS\Desktop\agriscan_plus_app\android-sdk
set ANDROID_HOME=C:\Users\IMS\Desktop\agriscan_plus_app\android-sdk
set PATH=%ANDROID_SDK_ROOT%\emulator;%ANDROID_SDK_ROOT%\platform-tools;%PATH%

REM Start emulator with network fixes
echo Starting emulator with network configuration...
emulator -avd Medium_Phone_API_36.1 -dns-server 8.8.8.8,8.8.4.4 -netdelay none -netspeed full -no-audio -no-boot-anim

pause
