// package de.ba.elos.vfbbo.cassandra.config;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.context.annotation.Primary;
// import org.springframework.context.annotation.Profile;
// import org.springframework.data.cassandra.SessionFactory;
// import org.springframework.data.cassandra.config.CqlSessionFactoryBean;
// import org.springframework.data.cassandra.config.SchemaAction;
// import org.springframework.data.cassandra.config.SessionFactoryFactoryBean;
// import org.springframework.data.cassandra.core.CassandraOperations;
// import org.springframework.data.cassandra.core.CassandraTemplate;
// import org.springframework.data.cassandra.core.convert.CassandraConverter;
// import org.springframework.data.cassandra.core.convert.MappingCassandraConverter;
// import org.springframework.data.cassandra.core.cql.CqlTemplate;
// import org.springframework.data.cassandra.core.mapping.CassandraMappingContext;
// import org.springframework.data.cassandra.core.mapping.SimpleUserTypeResolver;
// import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

// import com.datastax.oss.driver.api.core.ConsistencyLevel;
// import com.datastax.oss.driver.api.core.CqlSession;

// @Configuration
// @Profile("!test")
// @EnableCassandraRepositories(basePackages = {"de.aseno.spikes.order"})
// public class CassandraConfiguration { // extends AbstractCassandraConfiguration{

// 	private static final Logger LOGGER = LoggerFactory.getLogger(CassandraConfiguration.class);

// 	@Value("${spring.data.cassandra.keyspace-name}")
//     private String keyspace;


//     @Value("${spring.data.cassandra.contact-points}")
//     private String contactPoints;

//     @Value("${spring.data.cassandra.username}")
//     private String username;
//     @Value("${spring.data.cassandra.password}")
//     private String password;
//     @Value("${spring.data.cassandra.port}")
//     private Integer port;


//     @Value("${spring.data.cassandra.local-datacenter}")
//     private String localDataCenter;
    
//     @Value("${spring.data.cassandra.consistency-level}")
//     private ConsistencyLevel consistencyLevel;

//     @Value("${spring.data.cassandra.lower-consistency-level}")
//     private ConsistencyLevel lowerConsistencyLevel;

//     @Value("${spring.data.cassandra.number-of-retries}")
//     private int numberOfRetries;
    
//     @Value("${cassandra.connection.init.query.timeout}")
//     private long initQueryTimeout;

//     @Value("${cassandra.reconnect.on.init}")
//     private Boolean reconnectOnInit;

//     @Value("${cassandra.request.timeout}")
//     private long requestTimeout;

//     @Value("${cassandra.control.connection.timeout}")
//     private long controlConnectionTimeout;

//     @Value("${cassandra.metadata.schema.request.timeout}")
//     private long metadataRequestTimeout;
    

// //    @Bean
// //    public CqlTemplate cqlTemplate() {
// //
// //    	CqlTemplate template = new CqlTemplate();
// //    	
// //    	template.setConsistencyLevel(consistencyLevel);
// //    	template.setSerialConsistencyLevel(consistencyLevel);
// //    	template.setSessionFactory(new DefaultSessionFactory(session()));
// //    	return template;
// //    }
    
// PROBELM SERIAL ? how to change consitency level ?


// //  @Bean(name = "session")
// //  @Primary
// //  @Override
// //  public CqlSessionFactoryBean cassandraSession() {
// //      CqlSessionFactoryBean factory = super.cassandraSession();
// //      factory.setUsername(username);
// //      factory.setPassword(password);
// //      
// ////      factory.setPort(port);
// ////      factory.setKeyspaceName(keyspace);
// ////      factory.setContactPoints(contactPoints);
// ////      factory.setLocalDatacenter(localDataCenter);
// ////      factory.setSchemaAction(SchemaAction.CREATE_IF_NOT_EXISTS);
// ////      factory.setConverter(cassandraConverter());
// ////      factory.setSessionBuilderConfigurer(getSessionBuilderConfigurer()); // my session builder configurer 
// //     
// //      return factory;
// //  }
    



    
//   @Bean
// //  @Primary
//   public CqlSessionFactoryBean session() {

//     CqlSessionFactoryBean session = new CqlSessionFactoryBean();
//     session.setContactPoints(contactPoints);
//     session.setKeyspaceName(keyspace);
//     session.setLocalDatacenter(localDataCenter);
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
// //    @Override
// //    protected SessionBuilderConfigurer getSessionBuilderConfigurer() {
// //        return new SessionBuilderConfigurer() {
// //            @Override
// //            public CqlSessionBuilder configure(CqlSessionBuilder cqlSessionBuilder) {
// ////                ProgrammaticDriverConfigLoaderBuilder config = DriverConfigLoader.programmaticBuilder()
// ////                        .withDuration(DefaultDriverOption.CONNECTION_INIT_QUERY_TIMEOUT, Duration.ofSeconds(initQueryTimeout))
// ////                        .withBoolean(DefaultDriverOption.RECONNECT_ON_INIT, reconnectOnInit)
// ////                        .withDuration(DefaultDriverOption.REQUEST_TIMEOUT, Duration.ofSeconds(requestTimeout))
// ////                        .withDuration(DefaultDriverOption.CONTROL_CONNECTION_TIMEOUT, Duration.ofSeconds(controlConnectionTimeout))
// ////                        .withDuration(DefaultDriverOption.METADATA_SCHEMA_REQUEST_TIMEOUT, Duration.ofSeconds(metadataRequestTimeout));
// ////                
// //                return CqlSession.builder()
// //                .withLocalDatacenter(localDataCenter)
// //                .withAuthCredentials(username, password)
// //                
// //                .withKeyspace(keyspace)
// //                .addContactPoints(getAvailableContactPoints());
// //                // return cqlSessionBuilder.withAuthCredentials(username, password).withConfigLoader(config.build());
// //            }
// //        };
// //    }

    

    
// //    protected List<InetSocketAddress> getAvailableContactPoints() {
// //        List<String> nonAvailableclusterNodes = new ArrayList<>();
// //        List<InetSocketAddress> contactPointsTmp = Arrays
// //                .stream(contactPoints.split(","))
// //                .reduce(new ArrayList<>(), (res, address) -> {
// //                    String trimedAddress = address.trim();
// //                    try {
// //                        res.add(new InetSocketAddress(InetAddress.getByName(trimedAddress), getPort()));
// //                        return res;
// //                    } catch (UnknownHostException e) {
// //                        LOGGER.warn("Cassandra-Host nicht verfügbar: ".concat(trimedAddress), e);
// //                        nonAvailableclusterNodes.add(trimedAddress);
// //                        return res;
// //                    }
// //                }, (s, s1) -> s);
// //        if (nonAvailableclusterNodes.isEmpty()) {
// //            LOGGER.info("Alle Cassandra-Hosts sind verfügbar.");
// //        } else if (nonAvailableclusterNodes.size() == 1) {
// //            LOGGER.warn("Nicht alle Cassandra-Hosts sind verfügbar.");
// //        } else if (nonAvailableclusterNodes.size() > 1) {
// //            LOGGER.error("Nicht genug Cassandra-Host sind verfügbar");
// //            throw new RuntimeException("Nicht genug Cassandra-Host sind verfügbar");
// //        }
// //        return contactPointsTmp;
// //    }
    
// //    @Override
// //    protected String getContactPoints() {
// //        StringBuilder checkedContactPoints = new StringBuilder();
// //
// //        LOGGER.info("checking contactPoints=<{}> on port=<{}>", contactPoints, port);
// //
// //        for (String contactPoint : contactPoints.split(",")) {
// //            try {
// //                InetAddress.getAllByName(contactPoint);
// //                LOGGER.info("contactPoint=<{}> OK", contactPoint);
// //                checkedContactPoints.append(contactPoint).append(",");
// //            } catch (UnknownHostException e) {
// //                LOGGER.warn("contactPoint=<{}> unreachable: {}", contactPoint, e);
// //            }
// //        }
// //
// //        if (checkedContactPoints.length() == 0) {
// //            throw new IllegalStateException("all contactPoints are down");
// //        }
// //
// //        checkedContactPoints.deleteCharAt(checkedContactPoints.length() - 1);
// //
// //        return checkedContactPoints.toString();
// //    }

// //    @Override
// //    protected int getPort() {
// //        return port;
// //    }
// //    
// //    @Override
// //    protected String getLocalDataCenter() {
// //        return localDataCenter;
// //    }
// //    
// //    @Override
// //    protected String getKeyspaceName() {
// //        return keyspace;
// //    }
// //
// //    @Override
// //    public SchemaAction getSchemaAction() {
// //        return SchemaAction.CREATE_IF_NOT_EXISTS;
// //    }
// //
// //    @Override
// //    public String[] getEntityBasePackages() {
// //        return new String[]{"de.ba.elos.vfbbo.cassandra.model"};
// //    }

  
    
// //    @Override
// //    protected List<CreateKeyspaceSpecification> getKeyspaceCreations() {
// //        LOGGER.info("getKeyspaceCreations(): Keyspacename=<{}>", getKeyspaceName());
// //        final DataCenterReplication[] networkReplication = Arrays.stream(localDataCenter)
// //                .filter(StringUtils::hasText)
// //                .map(dataCenter -> DataCenterReplication.of(dataCenter, replicationFactor))
// //                .toArray(DataCenterReplication[]::new);
// //
// //        return Collections.singletonList(
// //                CreateKeyspaceSpecification.createKeyspace(getKeyspaceName())
// //                        .ifNotExists().withNetworkReplication(networkReplication));
// //    }


    

// //
// //    @Bean
// //    public SessionFactoryFactoryBean sessionFactory(CqlSession session, CassandraConverter converter) {
// //
// //      SessionFactoryFactoryBean sessionFactory = new SessionFactoryFactoryBean();
// //
// //      sessionFactory.setSession(session);
// //      sessionFactory.setConverter(converter);
// //      sessionFactory.setSchemaAction(SchemaAction.CREATE_IF_NOT_EXISTS);
// //
// //      return sessionFactory;
// //    }

// //    @Bean
// //    public CassandraMappingContext mappingContext(CqlSession cqlSession) {
// //
// //      CassandraMappingContext mappingContext = new CassandraMappingContext();
// //      mappingContext.setUserTypeResolver(new SimpleUserTypeResolver(cqlSession));
// //
// //      return mappingContext;
// //    }
// //



	
// }
