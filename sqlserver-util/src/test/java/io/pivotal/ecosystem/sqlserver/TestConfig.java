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

package io.pivotal.ecosystem.sqlserver;

import com.microsoft.sqlserver.jdbc.SQLServerConnectionPoolDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
@PropertySource("classpath:application.properties")
@Slf4j
class TestConfig {

    @Autowired
    private Environment env;

    @Bean
    public SqlServerClient client(JdbcTemplate jdbcTemplate, Environment env) {
        return new SqlServerClient(jdbcTemplate, dbUrl(env));
    }

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