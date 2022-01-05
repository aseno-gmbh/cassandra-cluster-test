# Cassandra-Cluster For Failover Tests

In this example we use docker-compose to assemble a 6 node-cassandra-cluster with 2+ rings/datacenters  on a single machine to run failover tests. The cluster could be easily extended to 10 or more nodes, depending on your requirements/ needs. It is advisable to have a machine with at least 16GB RAM.

Basic understanding of **Cassandra and Docker** is required to follow the steps bellow. If you are new to cassandra, please have a look at the [cassandra](https://cassandra.apache.org/doc/latest/) and/or [docker](https://docs.docker.com/compose/reference/) documentation.

## Set up the cluster

Using the [bitname/cassandra](https://https://hub.docker.com/r/bitnami/cassandra/) image and the file [docker-compose-cass-cluster.yaml](docker-compose-cass-cluster.yaml) (based on [CCC](https://github.com/digitalis-io/ccc)) we build up a cassandra cluster with 6 nodes and two rings. Note that the first three nodes {1,2,3} are bound to the datacenter/ring "rz1" and the second three {4,5,6} to the datacenter/ring "rz2".

An external docker-network called **cassnet** is used to share connectivity with the clients.

```bash
# create the internal network. It is used by clients to interact with the cluster
docker network create -d bridge cassnet

# run the 6 nodes cassandra cluster on this network. This may take some time. So please be patient ;-)
docker-compose -f docker-compose-cass-cluster.yaml up -d
```

Now we need to make some small changes to our new esablished cassandra-cluster.

```bash
# Go to one of the nodes using docker exec (with "docker ps" we can find the container id: 371cddb87cac) or in VSCODE --> attach shell
# 
docker exec -it cassandra1 bash

# now we should be on the node ;-) so we ca check the status of the cluster. It should look similar to the following output:
# of course we can also use "docker exec cassandra1 nodetool status"
I have no name!@62cdbadad6f1:/$ nodetool status
Datacenter: rz1
===============
Status=Up/Down
|/ State=Normal/Leaving/Joining/Moving
--  Address     Load       Tokens       Owns (effective)  Host ID                               Rack
UN  172.18.0.3  428.79 KiB  256          64.5%             737f400b-94ec-46a2-a6ee-3fa0e576cf30  rack1
UN  172.18.0.7  483.83 KiB  256          67.3%             b8e4918a-12ac-41ee-ae7e-09c856dda62c  rack1
UN  172.18.0.4  473.8 KiB  256          68.2%             b38129cc-c409-4e39-be4d-8a17c2479272  rack1
Datacenter: rz2
===============
Status=Up/Down
|/ State=Normal/Leaving/Joining/Moving
--  Address     Load       Tokens       Owns (effective)  Host ID                               Rack
UN  172.18.0.2  482.96 KiB  256          64.5%             e0fc50b5-6225-4df2-9265-aa0b59a936ff  rack2
UN  172.18.0.6  628.45 KiB  256          65.7%             d4febb11-cdbb-4ca0-b7ff-910077708101  rack2
UN  172.18.0.5  575.22 KiB  256          69.8%             8aa76583-d5a1-4669-a1b6-1f40d043dc56  rack2 

# connect with the default user: cassandra first
I have no name!@3017b7ebc38e:/$ cqlsh cassandra1 -u cassandra -p cassandra
Connected to aseno-cass at cassandra1:9042.
[cqlsh 5.0.1 | Cassandra 3.11.10 | CQL spec 3.4.4 | Native protocol v4]
Use HELP for help.
cassandra@cqlsh> 
# allow system auth 
cassandra@cqlsh> ALTER KEYSPACE "system_auth" WITH REPLICATION = {'class' : 'NetworkTopologyStrategy', 'rz1' : 2, 'rz2' : 2};

# create a new super user
cassandra@cqlsh> CREATE ROLE aseno with SUPERUSER = true AND LOGIN = true and PASSWORD = 'password';
# verify
cassandra@cqlsh> select peer, data_center, host_id, preferred_ip, rack, rpc_address from system.peers;

# create a new keyspace "myks" with NetworkTopologyStrategy and two rings rz1 and rz2 for our tests
CREATE KEYSPACE IF NOT EXISTS myks with replication = { 'class': 'NetworkTopologyStrategy', 'rz1': 2, 'rz2': 2};
# disconnect and run repair on the keyspace system_auth
nodetool repair system_auth
# done ;-)
```

## Set up & run a TEST-Application

Once again, we do not want to reinvent the wheel, so we borrow a simple [datastax/spring-data-cassandra example](https://github.com/DataStax-Examples/spring-data-starter) and make some adjustments ;-)

To run the application we need only to run docker-compose with the application file. Since we might want to attach a second test-application which will be connected with the different ring/datacenter, we use docker-compose again.

```bash
# run the test application
docker-compose -f docker-compose.yaml up -d

# see the logs
docker logs -f my-app
```

Now that the application is running, we can visit the swagger-ui under: <HOST>:<PORT>/swagger-ui/ e.g. http://localhost:8080/swagger-ui/. Here we can execute the usual GET,POST, etc. statements and create some content in the database. My personal recommendation is to use [postman](https://www.postman.com/) since it is one of the best tools for executing REST connamds.

At this point we need to make sure, that the application is doing the job properly. Since all cassandra-nodes are up and running,
we should be able to read/write with consistency level EACH_QUORUM. Have a look at the variable CASS_CL in the [docker-compose.yaml](docker-compose.yaml) file.

## Detaching a ring/datacenter

The easiest way to detach a datacenter is to disable the [cassandra-gossip](https://docs.datastax.com/en/cassandra-oss/3.0/cassandra/tools/toolsDisableGossip.html) protocol using the nodetool command disablegossip. This command effectively marks the node as being down.

```bash
# disable gossip on nodes 4,5,6 --> datacenter rz2 going down!
docker exec -it cassandra4 nodetool disablegossip
docker exec -it cassandra5 nodetool disablegossip
docker exec -it cassandra6 nodetool disablegossip

# verify that the ring is down --> DN (DOWN NORMAL)
 docker exec -it cassandra1 nodetool status
Datacenter: rz1
===============
Status=Up/Down
|/ State=Normal/Leaving/Joining/Moving
--  Address     Load       Tokens       Owns (effective)  Host ID                               Rack
UN  172.18.0.3  515.73 KiB  256          64.5%             737f400b-94ec-46a2-a6ee-3fa0e576cf30  rack1
UN  172.18.0.7  581.28 KiB  256          67.3%             b8e4918a-12ac-41ee-ae7e-09c856dda62c  rack1
UN  172.18.0.4  582.64 KiB  256          68.2%             b38129cc-c409-4e39-be4d-8a17c2479272  rack1
Datacenter: rz2
===============
Status=Up/Down
|/ State=Normal/Leaving/Joining/Moving
--  Address     Load       Tokens       Owns (effective)  Host ID                               Rack
DN  172.18.0.2  579.55 KiB  256          64.5%             e0fc50b5-6225-4df2-9265-aa0b59a936ff  rack2
DN  172.18.0.6  708.95 KiB  256          65.7%             d4febb11-cdbb-4ca0-b7ff-910077708101  rack2
DN  172.18.0.5  706.99 KiB  256          69.8%             8aa76583-d5a1-4669-a1b6-1f40d043dc56  rack2
```

If we now run the create order command (POST host:port/orders ...), we will see a datastax exception UnavailableException packaged within the spring-data
CassandraInsufficientReplicasAvailableException, since our application is still using a cinsistency level EACH_QUORUM.

```bash
[2022-01-04 14:49:55,233] 22397 [http-nio-8080-exec-1] ERROR o.a.c.c.C.[.[.[.[dispatcherServlet] - Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed; nested exception is org.springframework.data.cassandra.CassandraInsufficientReplicasAvailableException: Query; CQL [INSERT INTO starter_orders (order_id,product_id,added_to_order_at,product_name,product_price,product_quantity) VALUES (?,?,?,?,?,?)]; Not enough replicas available for query at consistency EACH_QUORUM (2 required but only 0 alive); nested exception is com.datastax.oss.driver.api.core.servererrors.UnavailableException: Not enough replicas available for query at consistency EACH_QUORUM (2 required but only 0 alive)] with root cause 
```

Now, go ahead an change the retry-policy from "com.datastax.oss.driver.internal.core.retry.DefaultRetryPolicy" to "de.aseno.spikes.ConsistencyRetryPolicy"
in the [docker-compose.yaml](docker-compose.yaml) file and run the same command again.

```bash
[2022-01-04 14:59:19,972] 17686 [http-nio-8080-exec-1] DEBUG o.s.d.c.core.cql.CqlTemplate - Executing prepared statement [INSERT INTO starter_orders (order_id,product_id,added_to_order_at,product_name,product_price,product_quantity) VALUES (?,?,?,?,?,?)] 
[2022-01-04 14:59:19,993] 17707 [my-sess-io-5] INFO  d.a.spikes.ConsistencyRetryPolicy - onUnavailableVerdict Alive:0 Retrycount:0 + CL: EACH_QUORUM 
[2022-01-04 14:59:19,994] 17708 [my-sess-io-5] WARN  d.a.spikes.ConsistencyRetryPolicy - EACH_QUORUM could not be reached -> Downgraded to LOCAL_QUORUM. Alive:0 Retrycount:0 
```

Our [RetryPolicy](src/main/java/de/aseno/spikes/ConsistencyRetryPolicy.java) adjusted the consistency level to make the insert possible.

## Retry Policy

How to deal with datacenter failure? datastax comes with two strategies DefaultRetryPolicy und  ConsistencyDowngradingRetryPolicy that could be extended with any "Customer"-RetryPolicies. That's what we did. See also [datastax-retries](https://docs.datastax.com/en/developer/java-driver/4.11/manual/core/retries/) and [retry-configuration](https://docs.datastax.com/en/developer/java-driver/4.11/manual/core/configuration/reference).

## Cassandra Repair

OK, we have inserted our data, but how are the datacenters/rings synchronized once the nodes 4,5,6 are up and running again? :-) Well, cassandra has two options - read repair and manuel repair (also anti-entropy repair). Have a look at: [Cassandra-Repair](https://cassandra.apache.org/doc/latest/cassandra/operating/repair.html).


## Clean up
To clean up the test application and the cassandra infrastructure go to the root folder and follow the steps bellow.

```bash
# remove the test application
docker-compose -f docker-compose.yaml down

# remove the cassandra cluster infrastructure
docker-compose -f docker-compose-cass-cluster.yaml down

# remove the docker volums (or use ./container/remove-stuff.sh)
docker  volume rm spring-cassandra_cassandra1_data
docker  volume rm spring-cassandra_cassandra2_data
docker  volume rm spring-cassandra_cassandra3_data
docker  volume rm spring-cassandra_cassandra4_data
docker  volume rm spring-cassandra_cassandra5_data
docker  volume rm spring-cassandra_cassandra6_data

# remove the cassnet docker network
docker network rm cassnet
```