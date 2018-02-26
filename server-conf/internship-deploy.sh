#!/bin/sh

EXECUTABLE=/root/internship-task/target/universal/stage/bin/internship
PID_FILE=/root/internship-task/target/universal/stage/RUNNING_PID
PID=`cat $PID_FILE`
BUILD_DIR=/root/internship-task
BUILD_CMD=activator
CWD=`pwd`

# Splash
echo "####  ##### ####  #      ###  #   #"
echo "#   # #     #   # #     #   # #   #"
echo "#   # ####  ####  #     #   #  ### "
echo "#   # #     #     #     #   #   #  "
echo "####  ##### #     #####  ###    #  "

# Stop the running application

/etc/init.d/play-internship stop

# Build the app

cd ${BUILD_DIR}
echo "Building app with 'activator clean compile stage'"
activator clean compile stage

# Copy logfile configuration

cp $BUILD_DIR/server-conf/internship-prod-logback.xml /opt/conf/play/internship-prod-logback.xml

# Start the server

/etc/init.d/play-internship start

# Return to previous path
cd ${CWD}