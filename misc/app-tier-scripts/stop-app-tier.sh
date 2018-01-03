#!/bin/bash
# location : /home/ec2-user/scripts

pid=`ps aux | grep "cloudpi-0.0.1-SNAPSHOT" | awk '{print $2}'`
kill -9 $pid