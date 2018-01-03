#!/bin/bash

web_tier_instance_id="i-05dcb7fbedd39f81d"
app_tier_1_instance_id="i-06cb5d399eba3a5f3"

#start web tier instance
printf "\nStarting Web Tier Instance...\n"
echo $(aws ec2 start-instances --instance-id $web_tier_instance_id)

#start app tier instance
printf "\nStarting App Tier Instance 1...\n"
echo $(aws ec2 start-instances --instance-id $app_tier_1_instance_id)

printf "\nEmptying S3 Bucket to capture data of only the current run...\n"
aws s3 rm s3://cloudpi-ram-suraj --recursive

printf "\nApplication Ready!\n"
printf "\nPublic IP : 34.208.190.32\n"


printf "\nSample : 34.208.190.32/cloudpi.php?input=1\n"


printf "\n[Heads Up: Its takes atleast 30 seconds for the instances to be UP and running.]\n"
# provide the public url and sample input