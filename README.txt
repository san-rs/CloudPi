Project : CloudPi
Members : Ramanathan Nachiappan (ASU ID : 1210822532)
		  Suraj Ravishankar (ASU ID : 1210973176)


Implementation:

A. ARCHITECTURE:
	1. We have implemented the prject in the form of a decoupled modular architecture
	2. Input from the client(browser) goes to php server. (cloudpi.php)
	3. The PHP server initiates web-tier Java project which picks up the input.
	4. It checks S3 if this input is already processed. 
	5. If yes, it will read the output of the corresponding input and send it back to php which sends it back to the client (OPTIMIZATION for CloudPi)
	6. If no, it will push the intput to SQS queue "cloudpi-input" and stores the corresponding message-id and starts polling the output SQS queue "cloudpi-output" using the message-id that it has stored.
	7. The App tier Java process (which fires at start up of the instance), keeps polling the input SQS queue "cloudpi-input" and picks up the input request that web-tier has enqueued.
	8. The app tier stores the message id of this message from the input queue and passes this input to pifft for processing and stores the output in S3, and now pushes the stored message id as a the message in the output queue "cloudpi-output"
	9. The web-tier which keeps polling the output queue for the message id of the request, will now receive the message id and realises the request has been processed by the app tier and thus reads the corresponding output from S3 and sends it to PHP which in turn sends it to the client (browser)
	10. Autoscaling process runs as a background process in the web tier and monitors two values 
		(i) number of pending messages in the input queue "cloudpi-input" - x
		(ii) number of running app instances - y
	11. If x is greater than y => subtract (x-y) and if its >=1 (this is because if there are 6 pending messages and there are 6 instances running, then in the next pick, each instance will pick up one request each simultaneously and hence we are not scaling up if the difference is less than 1)
	the application needs to be scaled up and thus creates a new app instance (and if the number of running app instances = 10, then scaling up will not be done)
	12. If x is lesser than y => the application needs to be scaled down and thus terminates the last created instance after verification that it is idle (performed by monitoring the CPU usage and its tag "isProcessing" which should be 0 when idle). This is again bound by the condition that number of running app instances should >=1 , and if this is not satisfied, scaling down will not be performed which ensures that atleast one instance is running all the time.


B. RESOURCES:
	1. 2 - 11 EC2 instances (based on auto scaling)
	2. 2 Queues in SQS (each for input and output)
		(i) cloudpi-input
		(ii) cloudpi-output
	3. 1 S3 bucket for storing the information
		"cloudpi-ram-suraj"
	4. 1 AMI - AMI of the app-tier EC2 instance
	5. 1 SECURITY GROUP "cloudpi"

C. STATS:
	-Without Autoscaling:
		Input - Time for processing
		1000000 - 2 mins
		1000001 - 2.6 mins
		1000002 - 2.7 mins
	-With Autoscaling:
		Input - Time for Processing
		1000003 - 36.65 seconds 
		1000004 - 1.6 mins
		1000005 - 2.6 mins