#!/usr/bin/env bash

usage="$0 [ blue | green ]\n-- Switch broker app to the selected color"
[[ $# != 1 ]] && { echo $usage; exit 1; }

echo_run()
{
    echo $@
    eval $@
}

#==============================================
# MAIN
#==============================================

case $1 in
       "green" )
           BROKER_START="greenplum-broker-green"
           BROKER_STOP="greenplum-broker-blue"
           ;;
       "blue" )
           BROKER_START="greenplum-broker-blue"
           BROKER_STOP="greenplum-broker-green"
           ;;
       * )
           echo $usage
           exit 1
           ;;
esac

echo_run "cf start $BROKER_START"

[[ $? == 0 ]] && echo_run "cf map-route $BROKER_START cfapps.io -n greenplum-pws"
[[ $? == 0 ]] && echo_run "cf unmap-route $BROKER_STOP cfapps.io -n greenplum-pws"
[[ $? == 0 ]] && echo_run "cf stop $BROKER_STOP"
