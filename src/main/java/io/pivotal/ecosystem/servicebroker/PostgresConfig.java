package io.pivotal.ecosystem.servicebroker;

import org.postgresql.ds.PGPoolingDataSource;
import org.springframework.cloud.servicebroker.model.BrokerApiVersion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

import static io.pivotal.ecosystem.servicebroker.PostgresClient.*;

@Configuration
@ComponentScan(basePackages = {"io.pivotal.ecosystem.servicebroker", "io.pivotal.cf.servicebroker", "io.pivotal.ecosystem.sqlserver"})
public class PostgresConfig {

    @Bean
    public DataSource datasource(Environment env) {
        PGPoolingDataSource source = new PGPoolingDataSource();
        source.setServerName(env.getProperty(POSTGRES_HOST_KEY));
        source.setDatabaseName(env.getProperty(POSTGRES_DB));
        source.setUser(env.getProperty(POSTGRES_USER));
        source.setPassword(env.getProperty(POSTGRES_PASSWORD));
        return source;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource datasource) {
        return new JdbcTemplate(datasource);
    }

    @Bean
    public String dbUrl(Environment env) {
        return POSTGRES_URI_SCHEME + "://" + env.getProperty(POSTGRES_HOST_KEY) + ":" + Integer.parseInt(env.getProperty(POSTGRES_PORT_KEY));
    }

    @Bean
    public BrokerApiVersion brokerApiVersion() {
        return new BrokerApiVersion();
    }
}