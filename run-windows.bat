@echo off
setlocal
cd /d "%~dp0"
echo ========================================
echo DIGIGUARD - Local College Demonstration
echo ========================================
echo.
where javac >nul 2>nul
if errorlevel 1 (
  echo ERROR: JDK not found. Install JDK 17 or newer and add it to PATH.
  pause
  exit /b 1
)
echo Compiling DIGIGUARD project...
javac Server.java WebServer.java Client.java CertGenerator.java
if errorlevel 1 (
  echo Compilation failed. Fix the errors above before running.
  pause
  exit /b 1
)
if not exist public.key if not exist private.key goto generatekeys
if not exist public.key goto missingkeys
if not exist private.key goto missingkeys
goto ready
:generatekeys
echo Generating a local RSA key pair for the first run...
java CertGenerator
if errorlevel 1 goto fail
:ready
echo.
echo Start the TCP authentication server in this window now.
echo Keep this window open. Open a SECOND Command Prompt in this folder and run:
echo   java WebServer
echo Then open index.html in your browser.
echo To test the Java client separately, set simulateFailure in Client.java and run java Client.
echo This launcher compiles and prepares keys; it does not start both servers automatically.
pause
exit /b 0
:missingkeys
echo ERROR: Only one key file exists. Restore the matching pair or back up and delete both, then run this script again.
goto fail
:fail
pause
exit /b 1
