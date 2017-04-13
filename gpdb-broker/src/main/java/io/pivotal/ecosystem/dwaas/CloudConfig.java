package io.pivotal.ecosystem.dwaas;

import io.pivotal.ecosystem.dwaas.connector.DWaaSServiceInfo;

import io.pivotal.ecosystem.servicebroker.model.ServiceBinding;
import io.pivotal.ecosystem.servicebroker.model.ServiceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.cloud.config.java.AbstractCloudConfig;
import org.springframework.cloud.servicebroker.model.BrokerApiVersion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
@Profile("cloud")
@ComponentScan(basePackages = {"io.pivotal.ecosystem.dwaas", "io.pivotal.ecosystem.servicebroker"})
public class CloudConfig {

    private static final Logger log = LoggerFactory.getLogger(CloudConfig.class);

    @Autowired
    private Environment environment;

    @Bean
    @Qualifier("JDBC")
    public DataSource datasource() throws Exception {
        String uri = environment.getProperty("spring.datasource.url");
        String user = environment.getProperty("spring.datasource.username");
        String password = environment.getProperty("spring.datasource.password");
        String driverClass = environment.getProperty("spring.datasource.driver-class-name");
        log.info("Got config: URL:{}, USER:{}, DRIVER:{}", uri, user, driverClass);

        DataSource dataSource;
        if ((uri != null) && (user != null)) {
            log.info("JDBC Datasource profile=[Provided]");
            log.info("DataWarehouse initial properties [URL:{}, UserName:{}, DriverClassName:{}]", uri, user,
                    driverClass);
            dataSource = DataSourceBuilder.create().driverClassName(driverClass).url(uri).username(user)
                    .password(password).build();
        } else {
            throw new Exception("Both URI and username must be provided.");
        }
        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(@Qualifier("JDBC") DataSource datasource) {
        return new JdbcTemplate(datasource);
    }

    @Bean
    public BrokerApiVersion brokerApiVersion() {
        return new BrokerApiVersion();
    }


}