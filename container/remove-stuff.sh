#!/bin/bash
echo "-------------  remove cass volumes --------------------------------------"
docker  volume rm cassandra-cluster-test_cassandra1_data 
docker  volume rm cassandra-cluster-test_cassandra2_data
docker  volume rm cassandra-cluster-test_cassandra3_data
docker  volume rm cassandra-cluster-test_cassandra4_data
docker  volume rm cassandra-cluster-test_cassandra5_data
docker  volume rm cassandra-cluster-test_cassandra6_data

echo "-------------  done --------------------------------------"
