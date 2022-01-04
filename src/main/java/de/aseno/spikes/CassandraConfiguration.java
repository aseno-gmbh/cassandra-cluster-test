package de.aseno.spikes;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.List;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.config.ProgrammaticDriverConfigLoaderBuilder;
import com.datastax.oss.driver.internal.core.auth.PlainTextAuthProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.config.CqlSessionFactoryBean;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.config.SessionBuilderConfigurer;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

@Configuration
@Profile("!test")
@EnableCassandraRepositories(basePackages = { "de.aseno.spikes.order" })
public class CassandraConfiguration extends AbstractCassandraConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(CassandraConfiguration.class);

  
    @Value("#{'${driver.contactPoints}'.split(',')}")
    protected List<String> contactPoints;
  
    @Value("${driver.port:9042}")
    protected int port;
  
    @Value("${driver.localdc}")
    protected String localDc;
  
    @Value("${driver.keyspace}")
    protected String keyspaceName;
  
    @Value("${driver.consistency:LOCAL_QUORUM}")
    protected String consistency;
    
    @Value("${driver.serialConsistency:LOCAL_SERIAL}")
    protected String serialConsistency;
    
    @Value("${driver.retryPolicy}")
    protected String retryPolicy;
  
    @Value("${driver.username}")
    protected String dseUsername;
  
    @Value("${driver.password}")
    protected String dsePassword;

    @Override
    protected String getContactPoints() {
        StringBuilder checkedContactPoints = new StringBuilder();

        LOGGER.info("checking contactPoints=<{}> on port=<{}>", contactPoints, port);

        for (String contactPoint : contactPoints) {
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
    protected String getKeyspaceName() {
        return keyspaceName;
    }

    @Override
    public SchemaAction getSchemaAction() {
        return SchemaAction.CREATE_IF_NOT_EXISTS;
    }

    @Override
    public String[] getEntityBasePackages() {
        return new String[] { "de.aseno.spikes.order" };
    }


    /**
   * Returns the keyspace to connect to. The keyspace specified here must exist.
   *
   * @return The {@linkplain CqlIdentifier keyspace} bean.
   */
  @Bean
  public CqlIdentifier keyspace() {
    return CqlIdentifier.fromCql(keyspaceName);
  }

  /**
   * Returns a {@link ProgrammaticDriverConfigLoaderBuilder} to load driver options.
   *
   * <p>Use this loader if you need to programmatically override default values for any driver
   * setting. In this example, we manually set the default consistency level to use, and, if a
   * username and password are present, we define a basic authentication scheme using {@link
   * PlainTextAuthProvider}.
   *
   * <p>Any value explicitly set through this loader will take precedence over values found in the
   * driver's standard application.conf file.
   *
   * @return The {@link ProgrammaticDriverConfigLoaderBuilder} bean.
   */
//   @Bean
//   public ProgrammaticDriverConfigLoaderBuilder configLoaderBuilder() {
//     ProgrammaticDriverConfigLoaderBuilder configLoaderBuilder =
//         DriverConfigLoader.programmaticBuilder()
//             .withString(DefaultDriverOption.REQUEST_CONSISTENCY, consistency);
//       configLoaderBuilder =
//           configLoaderBuilder
//               .withString(
//                   DefaultDriverOption.AUTH_PROVIDER_CLASS, PlainTextAuthProvider.class.getName())
//               .withString(DefaultDriverOption.AUTH_PROVIDER_USER_NAME, dseUsername)
//               .withString(DefaultDriverOption.SESSION_NAME, "my-sess")
//               .withString(DefaultDriverOption.AUTH_PROVIDER_PASSWORD, dsePassword);

//     return configLoaderBuilder;
//   }

  @Bean
  @Primary
  @Override
  public CqlSessionFactoryBean cassandraSession() {
      CqlSessionFactoryBean sessionFactory = new CqlSessionFactoryBean();
    //   sessionFactory.setUsername(dseUsername);
    //   sessionFactory.setPassword(dsePassword);
    //   sessionFactory.setPort(port);
      sessionFactory.setKeyspaceName(keyspaceName);
    //   sessionFactory.setContactPoints(getContactPoints());
    //   sessionFactory.setLocalDatacenter(localDc);
      sessionFactory.setSessionBuilderConfigurer(getSessionBuilderConfigurer());


      // cassandraSession.setSessionBuilderConfigurer(sessionBuilderConfigurer)
      // cassandraSession.setRetryPolicy(new ConsistencyRetryPolicy());
      return sessionFactory;
  }
  protected SessionBuilderConfigurer getSessionBuilderConfigurer() {
    return new SessionBuilderConfigurer() {
        @Override
        public CqlSessionBuilder configure(CqlSessionBuilder cqlSessionBuilder) {
            ProgrammaticDriverConfigLoaderBuilder config = DriverConfigLoader.programmaticBuilder()
                    .withDuration(DefaultDriverOption.CONNECTION_INIT_QUERY_TIMEOUT, Duration.ofSeconds(30))
                    .withBoolean(DefaultDriverOption.RECONNECT_ON_INIT, true)
                    .withDuration(DefaultDriverOption.REQUEST_TIMEOUT, Duration.ofSeconds(30))
                    .withDuration(DefaultDriverOption.CONTROL_CONNECTION_TIMEOUT, Duration.ofSeconds(20))
                    .withString(DefaultDriverOption.SESSION_NAME, "my-sess")
                    .withBoolean(DefaultDriverOption.REQUEST_DEFAULT_IDEMPOTENCE, true)
                    .withString(DefaultDriverOption.RETRY_POLICY_CLASS, retryPolicy)
                    .withString(DefaultDriverOption.REQUEST_CONSISTENCY, consistency)
                    .withString(DefaultDriverOption.REQUEST_SERIAL_CONSISTENCY, serialConsistency);
                    for (String contactPoint : contactPoints) {
                        InetSocketAddress address = InetSocketAddress.createUnresolved(contactPoint, port);
                        cqlSessionBuilder = cqlSessionBuilder.addContactPoint(address);
                    }
            return cqlSessionBuilder.withAuthCredentials(dseUsername, dsePassword)
            .withLocalDatacenter(localDc)
            // .withKeyspace(keyspaceName)
            .withConfigLoader(config.build());
        }
    };
  }
  /**
   * Returns a {@link CqlSessionBuilder} that will configure sessions using the provided {@link
   * ProgrammaticDriverConfigLoaderBuilder config loader builder}, as well as the contact points and
   * local datacenter name found in application.yml, merged with other options found in
   * application.conf.
   *
   * @param driverConfigLoaderBuilder The {@link ProgrammaticDriverConfigLoaderBuilder} bean to use.
   * @return The {@link CqlSessionBuilder} bean.
   */
//   @Bean
//   @Primary
//   public CqlSessionBuilder sessionBuilder(
//       @NonNull ProgrammaticDriverConfigLoaderBuilder driverConfigLoaderBuilder) {
//     CqlSessionBuilder sessionBuilder =
//         new CqlSessionBuilder().withConfigLoader(driverConfigLoaderBuilder.build());
//     for (String contactPoint : contactPoints) {
//       InetSocketAddress address = InetSocketAddress.createUnresolved(contactPoint, port);
//       sessionBuilder = sessionBuilder.addContactPoint(address);
//     }
//     return sessionBuilder.withLocalDatacenter(localDc);
//   }

  /**
   * Returns the {@link CqlSession} to use, configured with the provided {@link CqlSessionBuilder
   * session builder}. The returned session will be automatically connected to the given keyspace.
   *
   * @param sessionBuilder The {@link CqlSessionBuilder} bean to use.
   * @param keyspace The {@linkplain CqlIdentifier keyspace} bean to use.
   * @return The {@link CqlSession} bean.
   */
//   @Bean
//   public CqlSession session(
//       @NonNull CqlSessionBuilder sessionBuilder,
//       @Qualifier("keyspace") @NonNull CqlIdentifier keyspace) {
//     return sessionBuilder.withKeyspace(keyspace).build();
//   }

//   @Bean
//   public DriverConfigLoaderBuilderCustomizer defaultProfile(){
//       return builder -> builder.withString(DefaultDriverOption.METADATA_SCHEMA_REQUEST_TIMEOUT, "3 seconds").build();
//   }
 
    // @Bean
    // public DriverConfigLoaderBuilderCustomizer
    // driverConfigLoaderBuilderCustomizer() {
    // return builder -> builder.withBoolean(DefaultDriverOption.RECONNECT_ON_INIT,
    // true);
    // }

    // @Bean
    // public WriteOptions writeOptions() {
    //     return WriteOptions.builder().consistencyLevel(ConsistencyLevel.LOCAL_SERIAL)
    //             .withTracing() //
    //             .keyspace(CqlIdentifier.fromCql(keyspace)).build();
    // }

    // @Bean
    // public QueryOptions queryOptions() {
    //     return QueryOptions.builder()
    //             .consistencyLevel(ConsistencyLevel.LOCAL_QUORUM)
    //             // .retryPolicy(FallthroughRetryPolicy.INSTANCE)

    //             .tracing(true)
    //             .keyspace(CqlIdentifier.fromCql(keyspace)).build();
    //     // new QueryOptions()
    //     // .setMetadataEnabled(true)
    //     // .setDefaultIdempotence(true)
    //     // .setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
    // }

    // @Bean
    // @Primary
    // @Override
    // public CqlTemplate cqlTemplate() {

    //     CqlTemplate template = new CqlTemplate(); // super.cqlTemplate();

    //     template.setConsistencyLevel(DefaultConsistencyLevel.valueOf(consistencyLevel));
    //     template.setSerialConsistencyLevel(DefaultConsistencyLevel.LOCAL_SERIAL);
    //     template.setSession(super.getRequiredSession());
    //     template.afterPropertiesSet();
    //     return template;
    // }

    // @Bean
    // public CqlSessionBuilderCustomizer sessionBuilderConfigurer() {
    //     return cqlSessionBuilder -> cqlSessionBuilder
    //             .withAuthCredentials(username, password);
    // }

    // @Bean
    // @Primary
    // public DriverConfigLoaderBuilderCustomizer driverConfigLoaderBuilderCustomizer() {
    //     return loaderBuilder -> loaderBuilder
    //             .withDuration(DefaultDriverOption.REQUEST_TIMEOUT, Duration.ofMillis(10000))
    //             // .withBoolean(DefaultDriverOption.RETRY_POLICY, true)
    //             .withString(DefaultDriverOption.SESSION_KEYSPACE, "metest")
    //             .withString(DefaultDriverOption.SESSION_NAME, "my-sess")
    //             .withString(DefaultDriverOption.REQUEST_CONSISTENCY, "LOCAL_SERIAL")
    //             .withString(DefaultDriverOption.REQUEST_SERIAL_CONSISTENCY, "LOCAL_SERIAL");
    // }

}