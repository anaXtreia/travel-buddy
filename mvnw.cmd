@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF)
@REM ----------------------------------------------------------------------------
@REM Maven Start Up Batch script
@REM
@REM Required ENV vars:
@REM   JAVA_HOME - location of a JDK home dir
@REM
@REM Optional ENV vars
@REM   MAVEN_OPTS - parameters passed to the Java VM when running Maven
@REM ---------------------------------------------------------------------------

@IF "%__MVNW_ARG0_NAME__%"=="" (SET "BASE_DIR=%~dp0") ELSE (SET "BASE_DIR=%__MVNW_ARG0_NAME__%")

@SET MAVEN_PROJECTBASEDIR=%MAVEN_BASEDIR%
@IF NOT "%MAVEN_PROJECTBASEDIR%"=="" GOTO endDetectBaseDir

@SET EXEC_DIR=%CD%
@SET WDIR=%EXEC_DIR%
:findBaseDir
@IF EXIST "%WDIR%"\.mvn GOTO baseDirFound
@cd ..
@SET /P WDIR=<%CD%
@IF "%WDIR%"=="%CD%" GOTO baseDirNotFound
@GOTO findBaseDir

:baseDirFound
SET MAVEN_PROJECTBASEDIR=%WDIR%
CD "%EXEC_DIR%"
GOTO endDetectBaseDir

:baseDirNotFound
SET MAVEN_PROJECTBASEDIR=%EXEC_DIR%
CD "%EXEC_DIR%"

:endDetectBaseDir
SET WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
SET WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

SET DOWNLOAD_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.1/maven-wrapper-3.3.1.jar"

FOR /F "usebackq tokens=1,2 delims==" %%A IN ("%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties") DO (
    IF "%%A"=="wrapperUrl" SET DOWNLOAD_URL=%%B
)

@IF EXIST %WRAPPER_JAR% (
    @IF "%MVNW_VERBOSE%" == "true" (
        @echo Found %WRAPPER_JAR%
    )
) ELSE (
    @IF NOT "%MVNW_REPOURL%" == "" (
        SET DOWNLOAD_URL="%MVNW_REPOURL%/org/apache/maven/wrapper/maven-wrapper/3.3.1/maven-wrapper-3.3.1.jar"
    )
    @IF "%MVNW_VERBOSE%" == "true" (
        @echo Downloading %WRAPPER_JAR% from %DOWNLOAD_URL%
    )
    powershell -Command "&{"^
        "$webclient = new-object System.Net.WebClient;"^
        "if (-not ([string]::IsNullOrEmpty('%MVNW_USERNAME%') -and [string]::IsNullOrEmpty('%MVNW_PASSWORD%'))) {"^
        "$webclient.Credentials = new-object System.Net.NetworkCredential('%MVNW_USERNAME%', '%MVNW_PASSWORD%');"^
        "}"^
        "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; $webclient.DownloadFile('%DOWNLOAD_URL%', '%WRAPPER_JAR%')"^
        "}"
    if "%ERRORLEVEL%"=="0" GOTO execute
    del /F "%WRAPPER_JAR%" 2>NUL
    @echo Cannot download %WRAPPER_JAR%
    exit /b 1
)

:execute
@IF "%JAVA_HOME%"=="" (
  SET JAVACMD=java.exe
) ELSE (
  SET "JAVACMD=%JAVA_HOME%\bin\java.exe"
  IF NOT EXIST "%JAVACMD%" (
    ECHO Error: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
    ECHO Please set the JAVA_HOME variable correctly.
    EXIT /B 1
  )
)

@SET MAVEN_JAVA_EXE="%JAVACMD%"
SET WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

%MAVEN_JAVA_EXE% ^
  %MAVEN_OPTS% ^
  %MAVEN_DEBUG_OPTS% ^
  -classpath %WRAPPER_JAR% ^
  "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" ^
  %WRAPPER_LAUNCHER% %MAVEN_CMD_LINE_ARGS% %*
if ERRORLEVEL 1 goto error
goto end

:error
SET ERROR_CODE=1

:end
@endlocal & SET ERROR_CODE=%ERROR_CODE%
cmd /C exit /B %ERROR_CODE%
