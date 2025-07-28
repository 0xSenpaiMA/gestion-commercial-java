@echo off
echo Testing database connectivity...

rem Set classpath for SQLite JDBC
set SQLITE_JAR=sqlite-jdbc-3.44.1.0.jar

rem Create lib directory if it doesn't exist
if not exist lib mkdir lib

rem Download SQLite JDBC if not present
if not exist lib\%SQLITE_JAR% (
    echo Downloading SQLite JDBC driver...
    powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.44.1.0/sqlite-jdbc-3.44.1.0.jar' -OutFile 'lib\%SQLITE_JAR%'"
)

rem Compile the test
echo Compiling database test...
javac -cp "lib\%SQLITE_JAR%" -d target\classes src\main\java\com\gestioncommerciale\test\DatabaseTest.java

rem Run the test
echo Running database test...
java -cp "target\classes;lib\%SQLITE_JAR%" com.gestioncommerciale.test.DatabaseTest

pause
