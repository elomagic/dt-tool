#!/bin/bash

if [ -f $JAVA_HOME/bin/java ]
then
    export JAVACMD=$JAVA_HOME/bin/java
    APP_HOME=.
    CMD_LINE_ARGS=%*

    CLASS_LAUNCHER=de.elomagic.dttool.App

    $JAVACMD \
        -cp "$APP_HOME/target/*" \
        $CLASS_LAUNCHER $CMD_LINE_ARGS
else
    echo No Java runtime environment found.
    echo Please install Java at first and then try again.
fi

