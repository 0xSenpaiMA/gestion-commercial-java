@echo off
rem Maven wrapper script for Windows

set MAVEN_OPTS=-Xmx512m

rem Try to find Maven installation
if defined M2_HOME (
    set MVN_CMD=%M2_HOME%\bin\mvn.cmd
    goto :run
)

rem Try common Maven installation paths
if exist "C:\Program Files\Apache Software Foundation\maven\bin\mvn.cmd" (
    set MVN_CMD=C:\Program Files\Apache Software Foundation\maven\bin\mvn.cmd
    goto :run
)

if exist "C:\maven\bin\mvn.cmd" (
    set MVN_CMD=C:\maven\bin\mvn.cmd
    goto :run
)

rem Try to use mvn from PATH
where mvn.cmd >nul 2>&1
if %ERRORLEVEL% == 0 (
    set MVN_CMD=mvn.cmd
    goto :run
)

where mvn >nul 2>&1
if %ERRORLEVEL% == 0 (
    set MVN_CMD=mvn
    goto :run
)

echo Maven not found. Please install Maven or set M2_HOME environment variable.
exit /b 1

:run
echo Using Maven: %MVN_CMD%
%MVN_CMD% %*
