#!/bin/bash
start=$(date +%Y-%m-%dT%H:%M:%S -d "1 min ago")
end=$(date +%Y-%m-%dT%H:%M:%S -d "0 min ago")

#start=$(date -v -24H +%Y-%m-%dT%H:%M:%S)
#end=$(date +%Y-%m-%dT%H:%M:%S)

for i in $(aws ec2 describe-instances --query 'Reservations[*].Instances[0].[State.Name, InstanceId]' --output text | grep running | awk '{print$2}')
do
  echo $i
  echo $(aws cloudwatch get-metric-statistics --namespace AWS/EC2 --statistics Maximum --metric-name CPUUtilization --start-time=$start --end-time=$end --period 3600 --dimensions Name=InstanceId,Value=$i)
done

