package de.aseno.spikes;


import java.net.InetAddress;
import java.net.UnknownHostException;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.DefaultConsistencyLevel;
import com.datastax.oss.driver.api.core.config.DefaultDriverOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cassandra.DriverConfigLoaderBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.config.CqlSessionFactoryBean;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.core.cql.CqlTemplate;
import org.springframework.data.cassandra.core.cql.QueryOptions;
import org.springframework.data.cassandra.core.cql.WriteOptions;
import org.springframework.data.cassandra.core.cql.session.DefaultSessionFactory;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

@Configuration
@Profile("!test")
@EnableCassandraRepositories(basePackages = {"de.aseno.spikes.order"})
public class CassandraConfiguration  extends AbstractCassandraConfiguration{

    private static final Logger LOGGER = LoggerFactory.getLogger(CassandraConfiguration.class);
	
    @Value("${spring.data.cassandra.keyspace-name}")
    private String keyspace;


    @Value("${spring.data.cassandra.contact-points}")
    private String contactPoints;

    @Value("${spring.data.cassandra.request.consistency}")
    private String consistencyLevel;

    @Value("${spring.data.cassandra.port}")
    private Integer port;

    @Value("${spring.data.cassandra.local-datacenter}")
    private String localDataCenter;

    @Value("${spring.data.cassandra.username}")
    private String username;

    @Value("${spring.data.cassandra.password}")
    private String password;

//     // @Bean
//     // public CqlSessionBuilderCustomizer sessionBuilderCustomizer() {
//     // 	return builder -> builder
//     //             .addContactPoint(new InetSocketAddress(this.contactPoints, this.port))
//     //             .withLocalDatacenter(this.localDataCenter)
//     //             .with
//     //             .withAuthCredentials(this.username, this.password);
//     // }

//   @Bean
//   //@Primary
//   public CqlSessionFactoryBean session() {

//     CqlSessionFactoryBean session =   new CqlSessionFactoryBean(); // super.cassandraSession(); 
//     session.setContactPoints(contactPoints);
//     session.setKeyspaceName(keyspace);
//     session.setLocalDatacenter(localDataCenter);
//     // cassandraSession.setRetryPolicy(new ConsistencyRetryPolicy());
//     session.setUsername(username);
//     session.setPassword(password);

//     return session;
//   }

//   @Bean
//   @Primary
//   public SessionFactoryFactoryBean sessionFactory(CqlSession session, CassandraConverter converter) {

//     SessionFactoryFactoryBean sessionFactory = new SessionFactoryFactoryBean();
//     sessionFactory.setSession(session);
//     sessionFactory.setConverter(converter);
//     sessionFactory.setSchemaAction(SchemaAction.CREATE_IF_NOT_EXISTS);

//     return sessionFactory;
//   }

//   @Bean
//   @Primary
//   public CassandraMappingContext mappingContext(CqlSession cqlSession) {

//     CassandraMappingContext mappingContext = new CassandraMappingContext();
//     mappingContext.setUserTypeResolver(new SimpleUserTypeResolver(cqlSession));

//     return mappingContext;
//   }

//   @Bean
//   @Primary
//   public CassandraConverter converter(CassandraMappingContext mappingContext) {
//     return new MappingCassandraConverter(mappingContext);
//   }

//   @Bean
//   @Primary
//   public CassandraOperations cassandraTemplate(SessionFactory sessionFactory, CassandraConverter converter) {
// 	  CassandraTemplate cassTemplate = new CassandraTemplate(sessionFactory, converter);

//     return cassTemplate;
//   }
 
    /*
    *
    *          using ----------- AbstractCassandraConfiguration
    *
    */

    @Override
    protected String getContactPoints() {
        StringBuilder checkedContactPoints = new StringBuilder();

        LOGGER.info("checking contactPoints=<{}> on port=<{}>", contactPoints, port);

        for (String contactPoint : contactPoints.split(",")) {
            try {
                InetAddress.getAllByName(contactPoint);
                LOGGER.info("contactPoint=<{}> OK", contactPoint);
                checkedContactPoints.append(contactPoint).append(",");
            } catch (UnknownHostException e) {
                LOGGER.warn("contactPoint=<{}> unreachable: {}", contactPoint, e);
            }
        }

        if (checkedContactPoints.length() == 0) {
            throw new IllegalStateException("all contactPoints are down");
        }

        checkedContactPoints.deleteCharAt(checkedContactPoints.length() - 1);

        return checkedContactPoints.toString();
    }

    @Override
    protected int getPort() {
        return port;
    }
    
    @Override
    protected String getLocalDataCenter() {
        return localDataCenter;
    }

    @Override
    protected String getKeyspaceName() {
        return keyspace;
    }

    @Override
    public SchemaAction getSchemaAction() {
        return SchemaAction.CREATE_IF_NOT_EXISTS;
    }

    @Override
    public String[] getEntityBasePackages() {
        return new String[]{"de.aseno.spikes.order"};
    }
    @Bean
    @Override
    public CqlSessionFactoryBean cassandraSession() {
        CqlSessionFactoryBean cassandraSession = super.cassandraSession();
        cassandraSession.setUsername(username);
        cassandraSession.setPassword(password);
        
        // cassandraSession.setSessionBuilderConfigurer(sessionBuilderConfigurer)
        // cassandraSession.setRetryPolicy(new ConsistencyRetryPolicy());
        return cassandraSession;
    }
    // @Bean
    // public DriverConfigLoaderBuilderCustomizer driverConfigLoaderBuilderCustomizer() {
    //     return builder -> builder.withBoolean(DefaultDriverOption.RECONNECT_ON_INIT, true);
    // }
    
    @Bean
    public WriteOptions writeOptions() {
        return WriteOptions.builder().consistencyLevel(ConsistencyLevel.EACH_QUORUM)
        .withTracing() //
		.keyspace(CqlIdentifier.fromCql(keyspace)).build();
    }

    @Bean
    public QueryOptions queryOptions() {
        return QueryOptions.builder()
        .consistencyLevel(ConsistencyLevel.LOCAL_QUORUM)
        // .retryPolicy(FallthroughRetryPolicy.INSTANCE)

        .tracing(true)
        .keyspace(CqlIdentifier.fromCql(keyspace)).build();
        // new QueryOptions()
        // .setMetadataEnabled(true)
        // .setDefaultIdempotence(true)
        // .setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
       }

       @Bean
       @Override
       public CqlTemplate cqlTemplate() {
   
          CqlTemplate template = super.cqlTemplate(); //  CqlTemplate();
          
          template.setConsistencyLevel(DefaultConsistencyLevel.valueOf(consistencyLevel));
          template.setSerialConsistencyLevel(DefaultConsistencyLevel.valueOf(consistencyLevel));
          return template;
      }

}