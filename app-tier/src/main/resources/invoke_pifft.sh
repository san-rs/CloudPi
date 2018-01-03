#!/bin/bash

echo $1 > /home/ec2-user/cloudpi-cloud/c-pi/pifft/test.in
cd /home/ec2-user/cloudpi-cloud/c-pi/pifft
./pifft test.in > output.txt
cat output.txt