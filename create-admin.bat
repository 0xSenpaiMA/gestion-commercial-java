@echo off
echo Creating admin user...

rem Set up classpath with all required JARs
set CLASSPATH=target\classes
for %%f in (lib\*.jar) do set CLASSPATH=!CLASSPATH!;%%f

rem Enable delayed expansion for classpath building
setlocal enabledelayedexpansion

echo Compiling utility classes...
javac -cp "!CLASSPATH!" -d target\classes src\main\java\com\gestioncommerciale\util\AdminUserCreator.java

echo Running admin user creation...
java -cp "!CLASSPATH!" com.gestioncommerciale.util.AdminUserCreator

echo.
echo Admin user creation completed!
echo You can now login with: admin / admin123
pause
