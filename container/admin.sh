#!/bin/bash
echo "-------------  start --------------------------------------"
echo "-- clean package --"
cd ..
mvn clean package -DskipTests
cd container
echo "-- copy jar --"
rm -rf *.jar
cp ../target/*.jar .
echo "-- build image --"
docker rmi my-app
docker build -t my-app .
echo "-------------  done --------------------------------------"
