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
	private Environment env;

	@Bean
	@Qualifier("JDBC")
	public DataSource datasource() throws Exception {

        PoolProperties p = new PoolProperties();
        p.setUrl(env.getProperty("spring.datasource.url"));
        p.setDriverClassName(env.getProperty("spring.datasource.driver-class-name"));
        p.setUsername(env.getProperty("GPDB_USER"));
        p.setPassword(env.getProperty("GPDB_PASS"));
                		
        p.setInitialSize(env.getProperty("spring.datasource.tomcat.initial-size", Integer.class));
        p.setMaxActive(env.getProperty("spring.datasource.tomcat.max-active", Integer.class));
        p.setTestOnBorrow(env.getProperty("spring.datasource.tomcat.test-on-borrow", Boolean.class));
        p.setTestOnConnect(env.getProperty("spring.datasource.tomcat.test-on-connect", Boolean.class));
        p.setValidationQuery(env.getProperty("spring.datasource.tomcat.validation-query"));
        p.setMaxAge(env.getProperty("spring.datasource.tomcat.max-age", Integer.class));
        p.setRemoveAbandoned(env.getProperty("spring.datasource.tomcat.remove-abandoned", Boolean.class));
        p.setRemoveAbandonedTimeout(env.getProperty("spring.datasource.tomcat.remove-abandoned-timeout", Integer.class));

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
					+ " created_at timestamp with time zone not null default current_timestamp,"
					+ " last_access date default null,"
					+ " dropped_at timestamp with time zone default null,"
					+ " disabled_at timestamp with time zone default null )";

			jdbcTemplate.execute(sqlCmd);
		}
		return dataSource;
	}

	@Bean
	public BrokerApiVersion brokerApiVersion() {
		return new BrokerApiVersion();
	}
	
}
