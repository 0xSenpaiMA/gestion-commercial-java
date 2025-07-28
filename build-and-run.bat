@echo off
echo Building Gestion Commerciale Application...

rem Create necessary directories
if not exist target\classes mkdir target\classes
if not exist target\lib mkdir target\lib

rem Download required JAR files if they don't exist
echo Downloading dependencies...

if not exist target\lib\sqlite-jdbc-3.44.1.0.jar (
    echo Downloading SQLite JDBC...
    powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.44.1.0/sqlite-jdbc-3.44.1.0.jar' -OutFile 'target\lib\sqlite-jdbc-3.44.1.0.jar'"
)

if not exist target\lib\hibernate-core-6.3.1.Final.jar (
    echo Downloading Hibernate Core...
    powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/hibernate/hibernate-core/6.3.1.Final/hibernate-core-6.3.1.Final.jar' -OutFile 'target\lib\hibernate-core-6.3.1.Final.jar'"
)

if not exist target\lib\jakarta.persistence-api-3.1.0.jar (
    echo Downloading Jakarta Persistence API...
    powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/jakarta/persistence/jakarta.persistence-api/3.1.0/jakarta.persistence-api-3.1.0.jar' -OutFile 'target\lib\jakarta.persistence-api-3.1.0.jar'"
)

rem Set up classpath
set CLASSPATH=target\classes;target\lib\sqlite-jdbc-3.44.1.0.jar;target\lib\hibernate-core-6.3.1.Final.jar;target\lib\jakarta.persistence-api-3.1.0.jar

rem Compile main application classes first
echo Compiling application...

rem Compile entities
javac -cp "%CLASSPATH%" -d target\classes src\main\java\com\gestioncommerciale\model\*.java

rem Compile config (excluding the problematic SQLiteDialect)
javac -cp "%CLASSPATH%" -d target\classes src\main\java\com\gestioncommerciale\config\DatabaseConfig.java

rem Compile services
javac -cp "%CLASSPATH%" -d target\classes src\main\java\com\gestioncommerciale\service\*.java

rem Compile utils
javac -cp "%CLASSPATH%" -d target\classes src\main\java\com\gestioncommerciale\utils\*.java

rem Compile views
javac -cp "%CLASSPATH%" -d target\classes src\main\java\com\gestioncommerciale\view\*.java

rem Compile main app
javac -cp "%CLASSPATH%" -d target\classes src\main\java\com\gestioncommerciale\*.java

echo Compilation completed.

rem Copy resources
if not exist target\classes\META-INF mkdir target\classes\META-INF
copy src\main\resources\META-INF\persistence.xml target\classes\META-INF\
if exist src\main\resources\init-database.sql copy src\main\resources\init-database.sql target\classes\

echo Running database initialization...
java -cp "%CLASSPATH%" com.gestioncommerciale.service.SimpleDbInit

echo Starting application...
java -cp "%CLASSPATH%" com.gestioncommerciale.GestionCommercialeApp

pause
