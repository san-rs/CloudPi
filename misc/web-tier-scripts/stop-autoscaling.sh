#!/bin/bash
# location : /home/ec2-user/scripts

pid=`ps aux | grep "autoscaling.CloudpiAutoScaling" | awk '{print $2}'`
kill -9 $pid