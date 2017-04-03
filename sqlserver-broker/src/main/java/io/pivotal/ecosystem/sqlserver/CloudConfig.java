package io.pivotal.ecosystem.sqlserver;

import com.microsoft.sqlserver.jdbc.SQLServerConnectionPoolDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
@Profile("cloud")
@ComponentScan(basePackages = {"io.pivotal.ecosystem.servicebroker", "io.pivotal.cf.servicebroker", "io.pivotal.ecosystem.sqlserver"})
public class CloudConfig {

    @Bean
    public DataSource datasource(Environment env) {
        SQLServerConnectionPoolDataSource dataSource = new SQLServerConnectionPoolDataSource();

        dataSource.setURL(dbUrl(env));
        dataSource.setUser(env.getProperty(SqlServerClient.USER_KEY));
        dataSource.setPassword(env.getProperty(SqlServerClient.PW_KEY));

        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource datasource) {
        return new JdbcTemplate(datasource);
    }

    @Bean
    public String dbUrl(Environment env) {
        return SqlServerClient.URI_SCHEME + "://" + env.getProperty(SqlServerClient.HOST_KEY) + ":" + Integer.parseInt(env.getProperty(SqlServerClient.PORT_KEY));
    }
}
