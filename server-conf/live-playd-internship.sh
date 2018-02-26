#!/bin/sh
#
# Play Framework start/stop script, written by Ronnie Kilsbo
#
# Public Domain, use however you want to use it.
#
# PLAY CONFIGURATION
#
# Point this script to your Play application directory and the name of the
# application to be executed, as well as on which port to accept connections
# on. Status method can be either PID or HTML, PID checks only for process
# with the id in the RUNNING_PID file in the application directory while
# HTML checks for a string within the HTML url document.
#

PLAY_APPLICATION_NAME=internship-task
PLAY_APPLICATION_PATH=/root/internship-task/target/universal/stage
PLAY_PORT=80
PLAY_STATUS_METHOD=HTML
PLAY_HTML_CHECK_STRING="PONG"
PLAY_HTML_CHECK_URL=http://localhost:80/ping

# VARIABLES SETUP

PLAY_ARGS="-Dhttp.port=${PLAY_PORT} -Dlogger.file=/opt/conf/play/internship-prod-logback.xml -Dconfig.file=/opt/conf/play/live-application.conf"
PLAY_RUNNING_PID_FILE=${PLAY_APPLICATION_PATH}/RUNNING_PID
PLAY_EXECUTABLE=${PLAY_APPLICATION_PATH}/bin/${PLAY_APPLICATION_NAME}

# JAVA
export JAVA_HOME=/opt/java

# PATH
export PATH=$PATH:${JAVA_HOME}:/bin


# Kill the process
kill_play_application() {
    PID=`cat ${PLAY_RUNNING_PID_FILE}`

    # Start taking down Play application
    for sig in TERM HUP INT QUIT PIPE KILL; do
        if ! kill -${sig} "${PID}" > /dev/null 2>&1 ;
        then
            break
        fi
        sleep 2
    done
}

# Check if the play app is running by RUNNING_PID file
check_running_pid() {
    if [ -f ${PLAY_RUNNING_PID_FILE} ]; then
        if ps -p `cat ${PLAY_RUNNING_PID_FILE}` > /dev/null; then
            return 1
        fi
    fi
    return 0
}

# Check by matching the HTML
check_running_html() {
    curl -s ${PLAY_HTML_CHECK_URL} | grep -q "${PLAY_HTML_CHECK_STRING}"

    if [ $? -eq 0 ]; then
        return 1
    else
        return 0
    fi
}

# Do combination of checks
do_configured_check() {
    case "${PLAY_STATUS_METHOD}" in
        "HTML")
            check_running_pid & check_running_html
        ;;
        "PID")
            check_running_pid
        ;;
    esac
    if [ "$?" -eq "1" ]; then
        return 1
    else
        return 0
    fi
}

# Do some pre execution checks

if [ ! -f ]; then
    echo "Error: cannot find application executable: ${PLAY_EXECUTABLE}"
    exit 1
fi




# Execute
case "$1" in
    start)
        # Check pid / html
        do_configured_check

        case $? in
            1) echo "The application is already running with pid ${PLAY_RUNNING_PID_FILE}"; exit ;;
            2) rm ${PLAY_RUNNING_PID_FILE};;
        esac

        # Try starting the play application
        ${PLAY_EXECUTABLE} ${PLAY_ARGS} > /dev/null &
        sleep 2

        # Wait until application is started and notify
        matched=false
        while [ "${matched}" = "false" ]; do
            do_configured_check

            if [ $? -eq 1 ]; then
                echo "Play application is now started."
                matched=true
            else
                echo "Waiting for Play application to start..."
                sleep 5
            fi
        done
    ;;
    stop)
        check_running_pid

        case $? in
            0)
                echo "Not running."
            ;;
            1)
                kill_play_application
                echo "Stopped."
            ;;
        esac
    ;;
    restart)
        $0 stop
        $0 start
    ;;
    status)
        # Check pid / html
        do_configured_check

        case $? in
            0) echo "Play application NOT running" ;;
            1) echo "Play application ${PLAY_APPLICATION_NAME} is running";;
            2)
                rm ${PLAY_RUNNING_PID_FILE}
                echo "Play application wasn't running, found RUNNING_PID file that was removed"
            ;;
        esac
            ;;
        *)
            echo "Usage: $0 (start|stop|restart)"
    ;;
esac