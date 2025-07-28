@echo off
echo ===================================================
echo           Admin User Database Fix Utility
echo ===================================================
echo.

echo Compiling fix utility...
javac -cp "lib\sqlite-jdbc-3.44.1.0.jar" -d target\classes src\main\java\com\gestioncommerciale\util\FixAdminUser.java

if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Compilation failed
    pause
    exit /b 1
)

echo ✓ Compilation successful
echo.

echo Running admin user fix...
java -cp "target\classes;lib\sqlite-jdbc-3.44.1.0.jar" com.gestioncommerciale.util.FixAdminUser

echo.
echo ===================================================
echo Fix completed! You should now be able to login with:
echo   Username: admin
echo   Password: admin123
echo ===================================================
echo.
pause
