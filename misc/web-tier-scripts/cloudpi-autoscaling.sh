#!/bin/bash
# chkconfig: 1235 69 68
# description: Script to call the corresponding scripts in order to start and stop and the app tier code
#
# location : /etc/init.d

./etc/init.d/functions
#
if [ -f /etc/sysconfig/cloudpi-autoscaling ]; then
. /etc/sysconfig/cloudpi-autoscaling
fi
echo $1;

case $1 in
	start)
		/bin/bash /home/ec2-user/scripts/start-autoscaling.sh &
	;;
	stop)
		/bin/bash /home/ec2-user/scripts/stop-autoscaling.sh
	;;
	restart)
		/bin/bash /home/ec2-user/scripts/stop-autoscaling.sh
		/bin/bash /home/ec2-user/scripts/start-autoscaling.sh &
	;;
esac
#exit 0