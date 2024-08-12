@echo off

@setlocal

:OkJHome
set "JAVACMD=%JAVA_HOME%\bin\java.exe"

:checkJCmd
if exist "%JAVACMD%" goto chkHome

echo The JAVA_HOME environment variable is not defined correctly >&2
echo This environment variable is needed to run this program >&2
echo NB: JAVA_HOME should point to a JDK not a JRE >&2
goto error

:chkHome
set "APP_HOME=%~dp0"
if not "%APP_HOME%"=="" goto valHome
goto error

:valHome

:init
set CMD_LINE_ARGS=%*

:endInit

set CLASS_LAUNCHER=de.elomagic.dttool.App

"%JAVACMD%" ^
    -cp "%APP_HOME%\target\*" ^
    %CLASS_LAUNCHER% %CMD_LINE_ARGS%

goto end

:error
set ERROR_CODE=1

:end
@endlocal & set ERROR_CODE=%ERROR_CODE%
