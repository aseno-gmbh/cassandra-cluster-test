package de.aseno.spikes;


import java.net.InetSocketAddress;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer;
import org.springframework.context.annotation.Bean;



@SpringBootApplication

public class SpringDataCassandraApplication {

	
    @Value("${spring.data.cassandra.keyspace-name}")
    private String keyspace;


    @Value("${spring.data.cassandra.contact-points}")
    private String contactPoints;

    @Value("${spring.data.cassandra.port}")
    private Integer port;

    @Value("${spring.data.cassandra.local-datacenter}")
    private String localDataCenter;

    @Value("${spring.data.cassandra.username}")
    private String username;

    @Value("${spring.data.cassandra.password}")
    private String password;
	
    public static void main(String[] args) {
        SpringApplication.run(SpringDataCassandraApplication.class, args);
    }

    @Bean
    public CqlSessionBuilderCustomizer sessionBuilderCustomizer() {
    	return builder -> builder
                .addContactPoint(new InetSocketAddress(this.contactPoints, this.port))
                //.withLocalDatacenter(this.localDataCenter);
                .withAuthCredentials(this.username, this.password);
    }
}