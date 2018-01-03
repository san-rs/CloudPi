cloudpi.php
	-this is the php script that captures the input request from the browser
	-this script is hosted in an apache server

cloudpi.sh
	-this script is used to set the environment variables like JAVA_HOME and M2_HOME at start up of the instance and this was configured in "/etc/profile.d" location of the ec2 instance in order to run the script automatically by centos

initiate-autoscaling
	-this script is used to start the script "start-autoscaling.sh" in order the start Java process for autoscaling in the background at start of the web instance.
	-this script is configured using "chkconfig" command and the service was added to starter scripts and switched on, and hosted in the location "/etc/init.d"
	-using this script, every time an ec2 web instance starts, it will automatically initiate the java process in the background which will start the autoscaling process

start-autoscaling.sh
	-this script is used to start the java process by invoking the CloudpiAutoScaling class in the target folder of the maven project

stop-autoscaling.sh
	-this script is used to stop the java process by checking the background processes and killing the process by extracting its process ID