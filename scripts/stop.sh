#!/bin/bash
#extract all running instances and stop them

running_instances=$(aws ec2 describe-instances --query 'Reservations[*].Instances[*].[InstanceId]' --filters Name=instance-state-name,Values=running | grep -E -o "i\-[0-9A-Za-z]+")

for i in $running_instances
do
	echo $(aws ec2 stop-instances --instance-id $i)
done 