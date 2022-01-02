#!/bin/bash
echo "-------------  remove cass volumes --------------------------------------"
docker  volume rm spring-cassandra_cassandra1_data
docker  volume rm spring-cassandra_cassandra2_data
docker  volume rm spring-cassandra_cassandra3_data
docker  volume rm spring-cassandra_cassandra4_data
docker  volume rm spring-cassandra_cassandra5_data
docker  volume rm spring-cassandra_cassandra6_data

echo "-------------  done --------------------------------------"