#!/usr/bin/env bash

usage="$0 [ blue | green ] --dry-run\n-- Switch broker app to the selected color\n-- dry-run: outputs command without executing them"
[[ $# < 1 ]] && { echo -e $usage; exit 1; }
app_color=$1
[[ $# == 2 && $2 == '--dry-run'  ]] && doit=false || doit=true

echo_run()
{
    echo $@
    [[ $doit == true ]] && eval $@ || return 0
}

#==============================================
# MAIN
#==============================================

case $app_color in
       "green" )
           BROKER_START="greenplum-broker-green"
           BROKER_STOP="greenplum-broker-blue"
           ;;
       "blue" )
           BROKER_START="greenplum-broker-blue"
           BROKER_STOP="greenplum-broker-green"
           ;;
       * )
           echo -e $usage
           exit 1
           ;;
esac

echo_run "cf start $BROKER_START"

[[ $? == 0 ]] && echo_run "cf map-route $BROKER_START cfapps.io -n greenplum-pws"
[[ $? == 0 ]] && echo_run "cf unmap-route $BROKER_STOP cfapps.io -n greenplum-pws"
[[ $? == 0 ]] && echo_run "cf stop $BROKER_STOP"
