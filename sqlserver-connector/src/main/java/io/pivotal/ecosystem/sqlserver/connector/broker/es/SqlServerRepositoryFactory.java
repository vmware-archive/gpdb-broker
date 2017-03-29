/**
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

package io.pivotal.ecosystem.sqlserver.connector.broker.es;

import com.microsoft.sqlserver.jdbc.SQLServerConnectionPoolDataSource;
import com.microsoft.sqlserver.jdbc.SQLServerDriver;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbcp.BasicDataSource;

import javax.sql.DataSource;

@Slf4j
public class SqlServerRepositoryFactory {

    public DataSource create(SqlServerServiceInfo info) throws InterruptedException {
        BasicDataSource dataSource = new BasicDataSource();
        String s = SQLServerDriver.class.getName();

        dataSource.setDriverClassName(s);

        String url = info.getUri();
        dataSource.setUrl(url);
        log.info(" $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ "+ url + " $$$ " + info.getUser() + " &&&& " + info.getPassword() + " $$$ " + info.getDbname() );


        dataSource.setUsername(info.getUser());
        dataSource.setPassword(info.getPassword());
        return dataSource;

//
//        log.info("creating sqlServerRepository with info: " + info);
//
//        SQLServerConnectionPoolDataSource dataSource = new SQLServerConnectionPoolDataSource();
//        log.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% " + info.getUri());
//
//        dataSource.setURL(info.getUri());
//        dataSource.setUser(info.getUser());
//        dataSource.setPassword(info.getPassword());
//        if (info.getPort() != null) {
//            dataSource.setPortNumber(Integer.getInteger(info.getPort()));
//        } else {
//            dataSource.setPortNumber(1433);
//        }
//        dataSource.setDatabaseName(info.getDbname());
//
//        return dataSource;
    }
}
