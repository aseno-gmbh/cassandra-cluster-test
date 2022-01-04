package de.aseno.spikes;

import java.net.InetSocketAddress;
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

    @Value("${driver.sessionName}")
    protected String sessionName;

    @Value("${driver.username}")
    protected String dseUsername;

    @Value("${driver.password}")
    protected String dsePassword;

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

 
    @Bean
    @Primary
    @Override
    public CqlSessionFactoryBean cassandraSession() {
        CqlSessionFactoryBean sessionFactory = new CqlSessionFactoryBean();
        sessionFactory.setKeyspaceName(keyspaceName);
        sessionFactory.setSessionBuilderConfigurer(getSessionBuilderConfigurer());
        return sessionFactory;
    }

    /**
     * Returns a {@link SessionBuilderConfigurer} with a {@link ProgrammaticDriverConfigLoaderBuilder} to load driver options.
     *
     * <p>
     * This loader is used if we need to programmatically override default values for any driver
     * setting. In this example, we manually set the default consistency level to use, and, if a
     * username and password are present, we define a basic authentication scheme using {@link PlainTextAuthProvider}.
     *
     * <p>
     * Any value explicitly set through this loader will take precedence over values found in the
     * driver's standard application.conf file.
     * 
     * This example has been taken from see link below.
     *
     * @return The {@link SessionBuilderConfigurer} bean.
     * @see <a href="https://github.com/DataStax-Examples/cassandra-reactive-demo-java/blob/master/0_common/src/main/java/com/datastax/demo/common/conf/DriverConfiguration.java</a>
     */
    protected SessionBuilderConfigurer getSessionBuilderConfigurer() {
        LOGGER.info("use CONSISTENCY: " + consistency + " SERIAL_CONSISTENCY: " + serialConsistency + " RetryPolicy: " + retryPolicy);
        return new SessionBuilderConfigurer() {
            @Override
            public CqlSessionBuilder configure(CqlSessionBuilder cqlSessionBuilder) {
                ProgrammaticDriverConfigLoaderBuilder config = DriverConfigLoader.programmaticBuilder()
                        .withDuration(DefaultDriverOption.CONNECTION_INIT_QUERY_TIMEOUT, Duration.ofSeconds(30))
                        .withBoolean(DefaultDriverOption.RECONNECT_ON_INIT, true)
                        .withDuration(DefaultDriverOption.REQUEST_TIMEOUT, Duration.ofSeconds(30))
                        .withDuration(DefaultDriverOption.CONTROL_CONNECTION_TIMEOUT, Duration.ofSeconds(20))
                        .withString(DefaultDriverOption.SESSION_NAME, sessionName)
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
                        .withConfigLoader(config.build());
            }
        };
    }

}