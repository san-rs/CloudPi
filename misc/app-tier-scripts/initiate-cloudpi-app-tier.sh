#!/bin/bash
# chkconfig: 1235 69 68
# description: Script to call the corresponding scripts in order to start and stop and the app tier code
#
# location : /etc/init.d

./etc/init.d/functions
#
if [ -f /etc/sysconfig/initiate-cloudpi-app-tier ]; then
. /etc/sysconfig/initiate-cloudpi-app-tier
fi
echo $1;

case $1 in
	start)
		/bin/bash /home/ec2-user/scripts/start-app-tier.sh &
	;;
	stop)
		/bin/bash /home/ec2-user/scripts/stop-app-tier.sh
	;;
	restart)
		/bin/bash /home/ec2-user/scripts/stop-app-tier.sh
		/bin/bash /home/ec2-user/scripts/start-app-tier.sh &
	;;
esac
#exit 0