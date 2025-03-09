#!/bin/bash

if [ -f /usr/bin/java ]
then
    export JAVACMD=/usr/bin/java
    APP_HOME=.
    CMD_LINE_ARGS=%*

    LIBS_FOLDER=libs

    if test -d $APP_HOME/target; then
      LIBS_FOLDER=target
    fi

    CLASS_LAUNCHER=de.elomagic.dttool.App

    $JAVACMD \
        -cp "$APP_HOME/$LIBS_FOLDER/*" \
        $CLASS_LAUNCHER $CMD_LINE_ARGS
else
    echo No Java runtime environment found.
    echo Please install Java at first and then try again.
fi

