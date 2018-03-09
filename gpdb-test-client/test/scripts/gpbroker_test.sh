#!/bin/bash
#
# Script used to test the Greenplum service broker on PWS.
# Should be run periodically via cron. It performs the following:
#  1. Pushes the app if it not already running in the current org/space
#  2. Creates a Greenplum service and binds it to the app
#  3. Starts and stops the app
#  4. Unbinds and then deletes the service
#
# If any errors are encountered an email is sent to a team alias for
# correcting the issue.
#

log='/home/cf-user/broker-test-clients/logs/gpbroker-test.out'
#exec > ${log}
#exec 2>&1

function echo_run()
{
    echo $1
    eval $1
    return $?
}

function login_to_pcf()
{
    cf login -a https://api.run.pivotal.io -u '<CF USER>'  -o pivotal -s 'Data Engineering' -p '<USER PASSWORD>'
}

function build_manifest()
{
    cat << _MF > $1
---
applications:
- name: $APP_NAME
  memory: 1G
  instances: 1
  path: $APP_PATH
_MF

}

function create_and_bind_service()
{
    # First create the GP service for the app to bind to
    echo_run "cf create-service Greenplum Free $SERVICE_NAME"
    status=$?
    if [[ $status == 0 ]]; then
        echo_run "cf bind-service $APP_NAME $SERVICE_NAME"
        status=$?
    fi
    return $status
}

function unbind_and_delete_service()
{
    # First create the GP service for the app to bind to
    echo_run "cf unbind-service $APP_NAME $SERVICE_NAME"
    status=$?
    if [[ $status == 0 ]]; then
        echo_run "cf delete-service $SERVICE_NAME -f"
        status=$?
    fi
    return $status
}

function push_app()
{
    # Check if the app has been pushed yet ...

    status=0
    cf app $APP_NAME > /dev/null 2>&1
    if [[ $? != 0 ]]; then

	#build_manifest $MANIFEST

        # We push the app first without starting ...
        echo_run "cf push $APP_NAME -i 1 -m 1G -p target/gpdb-test-client-1.0.0.jar --no-start"
        if [[ $? != 0 ]]; then
            echo "FAIL: cf push of app '$APP_NAME' failed"
            echo "${MANIFEST}:"
            cat $MANIFEST
            echo '-------------------------------------------------------------------'
            echo_run "cf env $APP_NAME"
            echo '-------------------------------------------------------------------'
            echo_run "cf logs $APP_NAME --recent"
            status=1
        fi
        #rm -f $MANIFEST
     fi

     return $status
}

function start_and_stop_app()
{
    echo_run "cf restart $APP_NAME"
    status=$?
    if [[ $status == 0 ]]; then
        sleep 2
        echo_run "cf stop $APP_NAME"
        status=$?
    fi

    return $status
}

#################################################################################
# MAIN
#################################################################################

APP_DIR='/home/cf-user/broker-test-clients/gpdb-test-client'
APP_PATH='target/gpdb-test-client-1.0.0.jar'
APP_NAME='gpdb-test-client'
MANIFEST='./test-manifest.yml'
SERVICE_NAME='gpdb-test-service'

login_to_pcf

[[ ! -d $APP_DIR ]] && { echo "Dir '$APP_DIR' not found" ; exit 1 ; }
cd $APP_DIR
push_app
if [[ $? == 0 ]]; then
    # Try 3 times to create and bind the service
    for cnt in 1 2 3
    do
        create_and_bind_service
        status=$?
        [[ $status == 0 ]] && break
        sleep 5
    done
fi

if [[ $status == 0 ]]; then
    start_and_stop_app
    unbind_and_delete_service
    status=$?
fi

if [[ $status != 0 ]]; then
    saved_log=${log}.$(date +%Y%m%d-%H%M).txt
    mv ${log} ${saved_log}
    # Send an email to the team
    EMAIL_ADDR=data-cloud-dev@pivotal.io
    thisHost=$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4)
    if [ true ]; then
    mailx -s "GreenplumPWS Broker Test Failed" -r $EMAIL_ADDR -a ${saved_log} $EMAIL_ADDR << _EM
AWS Host: $thisHost
User: cf-user

The Greenplum Broker Test client ($APP_DIR) reported a failure. Please investigate.
Attached is a log of the cf 'create-service' and 'push' command outputs.
_EM
    fi
fi

exit 0
