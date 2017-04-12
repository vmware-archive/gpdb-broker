/*
 * Copyright (C) 2016-Present Pivotal Software, Inc. All rights reserved.
 * <p>
 * This program and the accompanying materials are made available under
 * the terms of the under the Apache License, Version 2.0 (the "License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.pivotal.ecosystem.dwaas.connector;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application.properties")
class TestConfig {

	private static final Logger log = LoggerFactory.getLogger(TestConfig.class);
	
    @Bean(name="testDatasource")
	public DataSource testdatasource() throws Exception {
    	log.info("Creating datasource bean");
    	
		/* jdbc:datadirect:greenplum://server1:5432;DatabaseName=jdbc;User=test;Password=secret */
		String uri = "jdbc:pivotal:greenplum://104.198.46.128:5432;DatabaseName=gpadmin;";
		String user = "gpadmin"; //props.getUsername();
		String password = "password"; //props.getPassword();

		String driverClass = "com.pivotal.jdbc.GreenplumDriver"; //props.getDriverClassName();

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
	
    /*
    @Bean
    public DWaaSServiceInfo sqlServerServiceInfo(Environment env) {
        return new DWaaSServiceInfo("1", env.getProperty(DWaaSServiceInfo.USER_KEY), env.getProperty(DWaaSServiceInfo.PW_KEY), DWaaSServiceInfo.URI_SCHEME + "://" + env.getProperty(DWaaSServiceInfo.HOST_KEY) + ":" + env.getProperty(DWaaSServiceInfo.PORT_KEY));
    }
    */
}