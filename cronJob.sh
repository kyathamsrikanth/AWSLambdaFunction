#!/bin/bash
cd /user/srikanthkyatham
java -cp "/user/srikanthkyatham:/user/srikanthkyatham/*" runLogGenerator
date=$(date '+%Y-%m-%d')
aws s3 cp /user/srikanthkyatham/log/LogFileGenerator.$date.log s3://cs441assignment2/logOutput/
