@echo off

@setlocal

:chkHome
set "APP_HOME=%~dp0"
if not "%APP_HOME%"=="" goto valHome
goto error

:valHome
set "JAVACMD=%APP_HOME%jre\bin\java.exe"

:init
set CMD_LINE_ARGS=%*

:endInit

set CLASS_LAUNCHER=de.elomagic.dttool.App

set libFolder=libs
if exist target (SET libFolder=target)

"%JAVACMD%" ^
    -cp "%APP_HOME%%libFolder%\*" ^
    %CLASS_LAUNCHER% %CMD_LINE_ARGS%

goto end

:error
set ERROR_CODE=1

:end
if not defined ERROR_CODE set ERROR_CODE=0
@endlocal & exit /b %ERROR_CODE%
