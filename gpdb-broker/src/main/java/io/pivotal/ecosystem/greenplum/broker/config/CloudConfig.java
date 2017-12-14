package io.pivotal.ecosystem.greenplum.broker.config;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.cloud.servicebroker.model.BrokerApiVersion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

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
		String uri = environment.getProperty("spring.datasource.url");
		String user = environment.getProperty("spring.datasource.username");
		String password = environment.getProperty("spring.datasource.password");
		String driverClass = environment.getProperty("spring.datasource.driver-class-name");
		logger.info("Got config: URL:{}, USER:{}, DRIVER:{}", uri, user, driverClass);

		DataSource dataSource;
		if ((uri != null) && (user != null)) {
			logger.info("JDBC Datasource profile=[Provided]");
			logger.info("DataWarehouse initial properties [URL:{}, UserName:{}, DriverClassName:{}]", uri, user,
					driverClass);
			dataSource = DataSourceBuilder.create().driverClassName(driverClass).url(uri).username(user)
					.password(password).build();
		} else {
			throw new Exception("Both URI and username must be provided.");
		}
		JdbcTemplate jdbcTemplate= new JdbcTemplate(dataSource);
		if (jdbcTemplate.queryForObject("SELECT count(*) FROM pg_class WHERE relname = ?", new Object[]{"service"}, Integer.class) == 0){
			String serviceTable = "CREATE TABLE service (serviceinstanceid varchar(200) not null default '',"
					+ " servicedefinitionid varchar(200) not null default '',"
					+ " planid varchar(200) not null default '',"
					+ " organizationguid varchar(200) not null default '',"
					+ " spaceguid varchar(200) not null default '')";

			jdbcTemplate.execute(serviceTable);
		}
		return dataSource;
	}

	@Bean
	public BrokerApiVersion brokerApiVersion() {
		return new BrokerApiVersion();
	}
	
}