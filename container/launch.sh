#!/bin/bash
# Start Service-Anwendung als Java-Anwendung
REST_SERVICE_JAR=mycass-0.0.1-SNAPSHOT.jar



# Start REST-Service-Anwendung als Java-Anwendung

LAUNCH=""
LAUNCH+=" java"

LAUNCH+=" -jar $REST_SERVICE_JAR"
LAUNCH+=" --spring.profiles.active=$ENV_PROFILES"



echo "launching application..."
exec $LAUNCH