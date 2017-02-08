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

package io.pivotal.ecosystem.sqlserver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.sql.*;

@Service
@Slf4j
public class SqlServerClient {

    private Environment env;

    public SqlServerClient(Environment env) {
        this.env = env;
    }

    public void execStatement(String statement) {
        Statement stmt = null;
        Connection conn = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            stmt.executeUpdate(statement);
            log.info("Database created successfully...");

        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException se2) {
                try {
                    if (conn != null)
                        conn.close();
                } catch (SQLException se) {
                    log.error(se.getMessage());
                }
            }
        }


    }


    public void createDatabase(String dbName) {
        execStatement("CREATE DATABASE " + dbName);
    }

    public void deleteDatabase(String dbName) {
        execStatement("DROP DATABASE " + dbName);
    }

    public boolean checkDatabaseExists(String dbName) {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT count(*) FROM sys.databases WHERE name = '" + dbName + "'");
            rs.next();
            if (rs.getInt(1) > 0) {
                return true;
            }
        } catch (Exception e) {
            log.error(e.getClass().getName() +" : " + e.getMessage());
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException se2) {
                try {
                    if (conn != null)
                        conn.close();
                } catch (SQLException se) {
                    log.error(se.getMessage());
                }
            }
        }
        return false;
    }

    public String getDbUrl() {

        String dbUrl = "jdbc:sqlserver://" + env.getProperty("HOST") + ":" + env.getProperty("PORT");
        return dbUrl;
    }

    public Connection getConnection() {

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            return DriverManager.getConnection(getDbUrl(), env.getProperty("USERNAME"), env.getProperty("PASSWORD"));
        } catch (Throwable throwable) {
            throw new ServiceBrokerException(throwable.getMessage());
        }


    }

}