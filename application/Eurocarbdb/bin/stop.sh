#!/bin/sh

if [ -z $ECDB_HOME ]; then
    ECDB_HOME=`pwd`
fi
echo "Application base directory $ECDB_HOME"
if [ -z $TOMCAT_BIN ]; then
    echo 'No TOMCAT_BIN variable set, searching /usr/ for the Tomcat bin directory'
    CATALINA_LOC=`find /usr/ -name 'catalina.sh' 2> /dev/null | grep 'bin/catalina.sh'`
    TOMCAT_BIN=`dirname "$CATALINA_LOC"`
    echo "Found Tomcat in $TOMCAT_BIN"
fi

export CATALINA_BASE="$ECDB_HOME"
export CATALINA_PID="$CATALINA_BASE/run/catalina.pid"
$TOMCAT_BIN/shutdown.sh
