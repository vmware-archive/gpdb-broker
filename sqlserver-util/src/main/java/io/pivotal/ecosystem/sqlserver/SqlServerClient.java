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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class SqlServerClient {

    private Environment env;
    public static final String USERNAME = "sqlserver_username";
    public static final String PASSWORD = "sqlserver_password";
    public static final String DB_PREFIX = "sqldb";
    public static final String DBNAME = "dbname";

    public SqlServerClient(Environment env) {
        this.env = env;
    }

    public void execStatement(String statement) {
        Statement stmt = null;
        Connection conn = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            stmt.execute(statement);
            log.info(statement + " statement executed successfully...");
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ServiceBrokerException(e);
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException se2) {
                try {
                    conn.close();
                } catch (SQLException se) {
                    log.warn(se.getMessage());
                }
            }
        }
    }


    public String cleanInstanceId(String instanceId) {
        return instanceId.replaceAll("[-]", "");
    }

    public String createdbName(String dbName) {
        return DB_PREFIX + cleanInstanceId(dbName);
    }

    public void createDatabase(String dbName) {
        String db = createdbName(dbName);
        execStatement("CREATE DATABASE " + db);
        log.info("Database: " + db + " created successfully...");
    }

    public void deleteDatabase(String dbName) {
        String db = createdbName(dbName);
        String deleteStmt = "ALTER DATABASE " + db + " SET SINGLE_USER WITH ROLLBACK IMMEDIATE; DROP DATABASE " + db;
        log.debug(deleteStmt);
        execStatement(deleteStmt);
        log.info("Database: " + db + " deleted successfully...");
    }

    public boolean checkDatabaseExists(String dbName) {
        return checkIfExists("SELECT count(*) FROM sys.databases WHERE name = '" + createdbName(dbName) + "'");
    }

    public String getDbUrl() {
        return "jdbc:sqlserver://" + env.getProperty("SQL_HOST") + ":" + env.getProperty("SQL_PORT");
    }

    public Connection getConnection() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            return DriverManager.getConnection(getDbUrl(), env.getProperty("SQLSERVER_USERNAME"), env.getProperty("SQLSERVER_PASSWORD"));
        } catch (Throwable throwable) {
            throw new ServiceBrokerException(throwable.getMessage());
        }
    }

    public Map<String, String> createUserCreds(String dbName) {

        Map<String, String> userCredentials = new HashMap<>();

        String uid = "user" + UUID.randomUUID().toString().replaceAll("[-]", "");
        String pwd = UUID.randomUUID().toString();

        userCredentials.put(USERNAME, uid);
        userCredentials.put(PASSWORD, pwd);

        String db = createdbName(dbName);

        String stmt = "CREATE LOGIN " + uid + " WITH PASSWORD = '" + pwd + "', DEFAULT_DATABASE = " + db + "; USE " + db + "; CREATE USER " + uid + " FOR LOGIN " + uid;
        execStatement(stmt);

        log.info("Created user: " + userCredentials.get(USERNAME));

        return userCredentials;
    }

    public void deleteUserCreds(String userName) {
        execStatement("DROP USER IF EXISTS " + userName + " ; DROP LOGIN " + userName);
    }

    public boolean checkUserExists(String username) {
        return checkIfExists("SELECT count(name) FROM sys.server_principals WHERE name = '" + username + "'");
    }

    private boolean checkIfExists(String countQuery) {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(countQuery);
            rs.next();
            if (rs.getInt(1) > 0) {
                return true;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ServiceBrokerException(e);
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException se2) {
                try {
                    conn.close();
                } catch (SQLException se) {
                    log.warn(se.getMessage());
                }
            }
        }
        return false;
    }
}