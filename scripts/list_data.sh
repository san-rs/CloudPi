#!/bin/bash
$(aws s3 cp s3://cloudpi-ram-suraj/ ./s3contents/ --recursive > s3execution.log)
for i in "./s3contents"/*
do
  #echo "$i"
  len=${#i}
  echo "Input : " ${i:13:$len} 
  printf "Output : "
  cat "$i"
  printf "\n\n"
done

rm s3execution.log
rm -r s3contents
