#!/bin/bash

AWS_PROFILE=default AWS_SDK_LOAD_CONFIG=true sam build && sam local start-api --port 4000 --profile default
