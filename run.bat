@echo off
echo Starting Gestion Commerciale Application...
echo.

rem Set Java options
set JAVA_OPTS=-Xmx1024m -Dfile.encoding=UTF-8

rem Check if Java is available
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo Java is not installed or not in PATH. Please install Java 17 or higher.
    pause
    exit /b 1
)

rem Try to compile with Maven if available
if exist "target\classes" goto :run

echo Compiling project...
if exist "mvnw.cmd" (
    call mvnw.cmd compile
) else (
    echo Maven not found. Please compile the project manually in your IDE.
    echo Press any key to try running anyway...
    pause >nul
)

:run
echo.
echo Starting application...
java %JAVA_OPTS% -cp "target\classes;target\dependency\*" com.gestioncommerciale.GestionCommercialeApp

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Application exited with error code %ERRORLEVEL%
    echo Please check the logs for more information.
    pause
)
