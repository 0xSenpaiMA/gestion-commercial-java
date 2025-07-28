@echo off
echo Building Gestion Commerciale Application...
echo.

rem Check if Java is available
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo Java is not installed or not in PATH. Please install Java 17 or higher.
    pause
    exit /b 1
)

rem Clean and compile
echo Cleaning previous build...
if exist "target" rmdir /s /q target

echo.
echo Compiling project...

rem Create target directories
mkdir target\classes 2>nul
mkdir target\dependency 2>nul

rem Simple compilation without Maven (for cases where Maven is not available)
echo Compiling Java sources...
javac -d target\classes -cp "lib\*" src\main\java\com\gestioncommerciale\*.java src\main\java\com\gestioncommerciale\*\*.java

if %ERRORLEVEL% EQU 0 (
    echo.
    echo Copying resources...
    xcopy /s /y src\main\resources\* target\classes\ >nul 2>&1
    
    echo.
    echo Build completed successfully!
    echo You can now run the application using run.bat
) else (
    echo.
    echo Build failed. Please check for compilation errors.
    echo Make sure all required dependencies are in the lib folder.
)

pause
