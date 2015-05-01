set ECDB_HOME="%1"
set CATALINA_OPTS="-Djava.awt.headless=true"
set CATALINA_BASE=%ECDB_HOME%
set CATALINA_HOME=%TOMCAT_DIR%
set CATALINA_PID=%CATALINA_BASE/logs/catalina.pid
set CATALINA_LOG=%CATALINA_BASE/logs/catalina.out
start %TOMCAT_DIR%\bin\catalina.bat %2