cloudpi.sh
	-this script is used to set the environment variables like JAVA_HOME and M2_HOME at start up of the instance and this was configured in "/etc/profile.d" location of the ec2 instance in order to run the script automatically by centos

initiate-cloudpi-app-tier.sh
	-this script is used to start the script "start-app-tier.sh" in order the start Java process in the background at start of the instance.
	-this script is configured using "chkconfig" command and the service was added to starter scripts and switched on, and hosted in the location "/etc/init.d"
	-using this script, every time an ec2 instance starts in the app tier, it will automatically initiate the java process in the background which will start polling the input queue for requests to be processed

start-app-tier.sh
	-this script is used to start the java process by invoking the CloudPiAppTier class in the target folder of the maven project

stop-app-tier.sj
	-this script is used to stop the java process by checking the background processes and killing the process by extracting its process ID