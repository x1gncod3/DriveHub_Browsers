@echo off
set ANDROID_SDK_ROOT=C:\Users\IMS\Desktop\agriscan_plus_app\android-sdk
set ANDROID_HOME=C:\Users\IMS\Desktop\agriscan_plus_app\android-sdk
set PATH=%ANDROID_SDK_ROOT%\emulator;%ANDROID_SDK_ROOT%\platform-tools;%PATH%

echo Starting emulator with DNS configuration...
emulator -avd Medium_Phone_API_36.1 -dns-server 8.8.8.8,8.8.4.4 -netdelay none -netspeed full

pause
