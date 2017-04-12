package io.pivotal.ecosystem.dwaas;

import io.pivotal.ecosystem.dwaas.connector.DWaaSServiceInfo;

import io.pivotal.ecosystem.servicebroker.model.ServiceBinding;
import io.pivotal.ecosystem.servicebroker.model.ServiceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
/*@ComponentScan(basePackages = { "io.pivotal.ecosystem.servicebroker", "io.pivotal.cf.servicebroker",
        "io.pivotal.ecosystem.dwaas" })*/
@ComponentScan(basePackages = {"io.pivotal.ecosystem.dwaas" })
@Profile("cloud")
public class CloudConfig extends AbstractCloudConfig{

    private static final Logger log = LoggerFactory.getLogger(CloudConfig.class);

	/*@Autowired
	DWaaSServiceInfo props;*/

    @Bean
    public DataSource datasource() throws Exception {

		/*String uri = props.getUri();
		String user = props.getUsername();
		String password = props.getPassword();*/
        String uri = "jdbc:pivotal:greenplum://104.198.46.128:5432;DatabaseName=gpadmin;";
        String user = "gpadmin";
        String password = "password";

        String driverClass = "com.pivotal.jdbc.GreenplumDriver";

        DataSource dataSource;
        if ((uri != null) && (user != null)) {
            log.info("JDBC Datasource profile=[Provided]");
            log.info("jdbcGemfireDataSource initial properties [URL:{}, UserName:{}, DriverClassName:{}]", uri, user,
                    driverClass);

            dataSource = DataSourceBuilder.create().driverClassName(driverClass).url(uri).username(user)
                    .password(password).build();
        } else {
            throw new Exception("Both URI and username must be provided.");
        }
        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource datasource) {
        return new JdbcTemplate(datasource);
    }

    @Bean
    public BrokerApiVersion brokerApiVersion() {
        return new BrokerApiVersion();
    }

    @Bean
    public RedisConnectionFactory redisFactory() {
        return connectionFactory().redisConnectionFactory();
    }

    @Bean
    RedisTemplate<String, ServiceInstance> instanceTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, ServiceInstance> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    @Bean
    RedisTemplate<String, ServiceBinding> bindingTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, ServiceBinding> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }
}