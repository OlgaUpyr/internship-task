#!/bin/sh

# Configuration
SERVER_CONF_SOURCE=/root/internship-task/server-conf
SOURCE_ROOT=/root/internship-task
GIT_REPOSITORY=$SOURCE_ROOT/.git
GIT_REPOSITORY_FILES=$SOURCE_ROOT

# Splash
echo "####  #   # #     #    "
echo "#   # #   # #     #    "
echo "####  #   # #     #    "
echo "#     #   # #     #    "
echo "#      ###  ##### #####"

# Check parameters
if [ "$#" -lt 2 ]
then
    echo Usage: internship-pull \<branch_name\> \<server_type \(stage \| live\)\>
    echo Deploying requires that you give the branch name and type of server
    echo as an option.
    exit 1
fi

if [ "$2" != "stage" ] && [ "$2" != "live" ]
then
    echo Server type: should be \<stage\> or \<live\>
    exit 1
fi

# Pull changes
git --work-tree=$GIT_REPOSITORY_FILES --git-dir=$GIT_REPOSITORY pull --all
git --work-tree=$GIT_REPOSITORY_FILES --git-dir=$GIT_REPOSITORY checkout $1
git --work-tree=$GIT_REPOSITORY_FILES --git-dir=$GIT_REPOSITORY pull

cp $SERVER_CONF_SOURCE/internship-deploy.sh /opt/bin/internship-deploy
cp $SERVER_CONF_SOURCE/internship-pull.sh /opt/bin/internship-pull
cp $SERVER_CONF_SOURCE/internship-prod-logback.xml /opt/conf/play/internship-prod-logback.xml


if [ "$2" == "stage" ]
then
    echo "Copying stage server configuration"
    cp $SERVER_CONF_SOURCE/stage-application.conf /opt/conf/play/internship-application.conf
    cp $SERVER_CONF_SOURCE/stage-playd-internship.sh /etc/init.d/play-internship
else
    echo "Copying live server configuration"
    cp $SERVER_CONF_SOURCE/live-application.conf /opt/conf/play/internship-application.conf
    cp $SERVER_CONF_SOURCE/live-playd-internship.sh /etc/init.d/play-internship
fi

chmod 700 /opt/bin/internship*