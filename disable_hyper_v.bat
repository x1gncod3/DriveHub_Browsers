@echo off
echo Disabling Hyper-V to fix emulator conflicts...
echo This requires administrator privileges.

REM Disable Hyper-V features
dism /online /disable-feature /featurename:Microsoft-Hyper-V-All /norestart
dism /online /disable-feature /featurename:HypervisorPlatform /norestart

echo Features disabled. You need to restart your computer.
echo After restart, try the emulator again.
pause
