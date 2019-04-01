#!/bin/bash

sam deploy --template-file packaged.yaml --stack-name ccm-sermons --capabilities CAPABILITY_IAM --region eu-west-2
