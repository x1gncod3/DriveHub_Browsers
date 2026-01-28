@echo off
echo Setting DNS for running emulator...
adb shell "setprop net.dns1 8.8.8.8"
adb shell "setprop net.dns2 8.8.4.4"
echo DNS configured! Testing connection...
adb shell ping -c 2 8.8.8.8
pause
