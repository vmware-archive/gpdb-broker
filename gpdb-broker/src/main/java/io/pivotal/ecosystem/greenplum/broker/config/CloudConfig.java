package io.pivotal.ecosystem.greenplum.broker.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.servicebroker.model.BrokerApiVersion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

@Configuration
@Profile("cloud")
@ComponentScan(basePackages = { "io.pivotal.ecosystem.greenplum.broker.database" })
public class CloudConfig {

	private static final Logger logger = LoggerFactory.getLogger(CloudConfig.class);

	@Autowired
	private Environment environment;

	@Bean
	@Qualifier("JDBC")
	public DataSource datasource() throws Exception {

        PoolProperties p = new PoolProperties();
        p.setUrl(environment.getProperty("spring.datasource.url"));
        p.setDriverClassName(environment.getProperty("spring.datasource.driver-class-name"));
        p.setUsername(environment.getProperty("spring.datasource.username"));
        p.setPassword(environment.getProperty("spring.datasource.password"));
                		
        p.setTestOnBorrow(environment.getProperty("spring.datasource.tomcat.test-on-borrow", Boolean.class));
        p.setTestOnConnect(environment.getProperty("spring.datasource.tomcat.test-on-connect", Boolean.class));
        p.setValidationQuery(environment.getProperty("spring.datasource.tomcat.validation-query"));
        p.setInitialSize(environment.getProperty("spring.datasource.tomcat.initial-size", Integer.class));
        p.setMaxActive(environment.getProperty("spring.datasource.tomcat.max-active", Integer.class));

        DataSource dataSource = new DataSource();
        dataSource.setPoolProperties(p);
        
        logger.info("datasource:" + dataSource + "\n");
		
		JdbcTemplate jdbcTemplate= new JdbcTemplate(dataSource);
        String tableName = "gpbroker_service";
        String sqlCmd = "";

        sqlCmd = "SELECT count(1) FROM pg_class WHERE relname = '" + tableName + "'";
		if (jdbcTemplate.queryForObject(sqlCmd, Integer.class) == 0) {
			sqlCmd = "CREATE TABLE " + tableName + " ("
					+ " service_instance_id varchar(200) not null default '',"
					+ " service_definition_id varchar(200) not null default '',"
					+ " plan_id varchar(200) not null default '',"
					+ " organization_guid varchar(200) not null default '',"
					+ " space_guid varchar(200) not null default '',"
					+ " created_at timestamp not null default current_timestamp,"
					+ " last_access date default null )";

			jdbcTemplate.execute(sqlCmd);
		}
		return dataSource;
	}

	@Bean
	public BrokerApiVersion brokerApiVersion() {
		return new BrokerApiVersion();
	}
	
}
