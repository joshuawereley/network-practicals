#!/bin/sh
export JAVA_HOME=/opt/homebrew/opt/openjdk
export PATH=$JAVA_HOME/bin:$PATH
/opt/homebrew/opt/openjdk/bin/java -cp /opt/homebrew/var/www/cgi-bin DisplayTime
