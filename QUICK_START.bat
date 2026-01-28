@echo off
echo ========================================
echo AABROWSER QUICK START GUIDE
echo ========================================
echo.
echo NETWORK ISSUE: Android emulator has DNS problems
echo SOLUTION: Start emulator with proper DNS settings
echo.
echo OPTION 1: Use Android Studio AVD Manager
echo 1. Open Android Studio
echo 2. Tools ^> AVD Manager
echo 3. Click Edit (pencil icon) on Medium_Phone_API_36.1
echo 4. Advanced Settings ^> Custom DNS: 8.8.8.8,8.8.4.4
echo 5. Save and Start
echo.
echo OPTION 2: Manual Command (if SDK path is correct)
echo emulator -avd Medium_Phone_API_36.1 -dns-server 8.8.8.8,8.8.4.4
echo.
echo OPTION 3: Test with Local Files (No Network Needed)
echo The browser already has error handling for network issues.
echo Test with: file:///sdcard/Download/test.html
echo.
echo CURRENT STATUS: 
echo - App code is ready
echo - Error handling works correctly  
echo - Just need emulator with internet access
echo.
echo ========================================

pause
