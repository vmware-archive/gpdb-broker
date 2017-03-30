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

    public static final String USERNAME = "uid";
    public static final String PASSWORD = "pw";
    public static final String DATABASE = "db";
    public static final int DEFAULT_PORT = 1433;

    public SqlServerClient(Environment env) {
        this.env = env;
    }

    private void execStatement(String statement) {
        Statement stmt = null;
        Connection conn = null;
        try {
            conn = getSAConnection();
            stmt = conn.createStatement();
            stmt.execute(statement);
            log.info(statement + " statement executed successfully...");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
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

    String createDatabase() {
        String db = createDbName();

        execStatement("use [master]");
        execStatement("exec sp_configure 'contained database authentication', 1");
        execStatement("reconfigure");
        execStatement("CREATE DATABASE [" + db + "]");
        execStatement("ALTER DATABASE [" + db + "] SET CONTAINMENT = PARTIAL");

//        execStatement("use " + db);
//        execStatement("exec sp_configure 'contained database authentication', 1");


        log.info("Database: " + db + " created successfully...");
        return db;
    }

    void deleteDatabase(String db) {
        String deleteStmt = "ALTER DATABASE " + db + " SET SINGLE_USER WITH ROLLBACK IMMEDIATE; DROP DATABASE " + db;
        log.debug(deleteStmt);
        execStatement(deleteStmt);
        log.info("Database: " + db + " deleted successfully...");
    }

    boolean checkDatabaseExists(String db) {
        return checkIfExists("SELECT count(*) FROM sys.databases WHERE name = '" + db + "'");
    }

    String getDbUrl(String db) {
        String uri = null;
        if (db == null) {
            uri = "jdbc:sqlserver://" + getHost() + ":" + getPort();
        } else {
            uri = "jdbc:sqlserver://" + getHost() + ":" + getPort() + ";databaseName=" + db;
        }
        log.info("**************************** " + uri + " +++++++++++++++++++++++++++++++++++");
        return uri;
    }

    String getHost() {
        return env.getProperty("SQL_HOST");
    }

    int getPort() {
        if (env.getProperty("SQL_PORT") != null) {
            return Integer.parseInt(env.getProperty("SQL_PORT"));
        }
        return DEFAULT_PORT;
    }

    Connection getSAConnection() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            return DriverManager.getConnection(getDbUrl(null), env.getProperty("SQLSERVER_USERNAME"), env.getProperty("SQLSERVER_PASSWORD"));
        } catch (Throwable throwable) {
            throw new ServiceBrokerException(throwable.getMessage());
        }
    }

    Connection getUserConnection(String uid, String pw, String db) {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            return DriverManager.getConnection(getDbUrl(db), uid, pw);
        } catch (Throwable throwable) {
            throw new ServiceBrokerException(throwable.getMessage());
        }
    }

    private String getRandomishId() {
        return UUID.randomUUID().toString().replaceAll("[-]", "");
    }

    private String createUserId() {
        return "u" + getRandomishId();
    }

    private String createPassword() {
        return "P" + getRandomishId();
    }

    String createDbName() {
        return "d" + getRandomishId();
    }

    Map<String, String> createUserCreds(String db) {

        Map<String, String> userCredentials = new HashMap<>();

        String uid = createUserId();
        String pwd = createPassword();

        userCredentials.put(USERNAME, uid);
        userCredentials.put(PASSWORD, pwd);
        userCredentials.put(DATABASE, db);

        log.info("creds: " + userCredentials.toString());

        String s = " USE [" + db + "]; CREATE USER [" + uid + "] WITH PASSWORD='" + pwd + "', DEFAULT_SCHEMA=[dbo];";
        execStatement(s);

////        execStatement("CREATE LOGIN " + uid + " WITH PASSWORD = '" + pwd + "', DEFAULT_DATABASE = " + db);
//        execStatement("USE " + db);
////        execStatement("CREATE USER " + uid + " FOR LOGIN " + uid);
////        execStatement("CREATE USER " + uid);
//
//        execStatement("CREATE USER " + uid + " with password = '" + pwd + "';");
////        execStatement("EXEC sp_addrolemember 'db_owner', '" + uid + "';");

        log.info("Created user: " + userCredentials.get(USERNAME));

        return userCredentials;
    }

    void deleteUserCreds(String uid) {
        execStatement("DROP USER IF EXISTS " + uid + " ; DROP LOGIN " + uid);
    }

    boolean checkUserExists(String uid) {
        return checkIfExists("SELECT count(name) FROM sys.server_principals WHERE name = '" + uid + "'");
    }

    private boolean checkIfExists(String countQuery) {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = getSAConnection();
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(countQuery);
            rs.next();
            if (rs.getInt(1) > 0) {
                return true;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
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