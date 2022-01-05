#!/bin/bash
# Start the java test application
REST_SERVICE_JAR=mycass-1.0.0.jar

LAUNCH=""
LAUNCH+=" java"

LAUNCH+=" -jar $REST_SERVICE_JAR"
# use spring profile 
LAUNCH+=" --spring.profiles.active=$ENV_PROFILES"

echo "launching application..."
exec $LAUNCH