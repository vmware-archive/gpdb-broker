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

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
class SqlServerClient {

    //todo literals
    static final String USERNAME = "uid";
    static final String PASSWORD = "pw";
    static final String DATABASE = "db";
    static final String HOST_KEY = "SQL_HOST";
    static final String PORT_KEY = "SQL_PORT";
    static final String USER_KEY = "SQLSERVER_USERNAME";
    static final String PW_KEY = "SQLSERVER_PASSWORD";
    static final String URI_SCHEME = "jdbc:sqlserver";

    private JdbcTemplate jdbcTemplate;
    private String url;

    SqlServerClient(JdbcTemplate jdbcTemplate, String dbUrl) {
        this.jdbcTemplate = jdbcTemplate;
        this.url = dbUrl;
    }

    String createDatabase() {
        String db = createDbName();
        jdbcTemplate.execute("use [master]; exec sp_configure 'contained database authentication', 1 reconfigure; CREATE DATABASE [" + db + "]; ALTER DATABASE [" + db + "] SET CONTAINMENT = PARTIAL");
        log.info("Database: " + db + " created successfully...");
        return db;
    }

    void deleteDatabase(String db) {
        jdbcTemplate.execute("ALTER DATABASE " + db + " SET SINGLE_USER WITH ROLLBACK IMMEDIATE; DROP DATABASE " + db);
        log.info("Database: " + db + " deleted successfully...");
    }

    boolean checkDatabaseExists(String db) {
        return jdbcTemplate.queryForObject("SELECT count(*) FROM sys.databases WHERE name = ?", new Object[]{db}, Integer.class) > 0;
    }

    //todo literals
    String getDbUrl(String db) {
        if (db == null) {
            return this.url;
        }
        return this.url + ";databaseName=" + db;
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

    private String createDbName() {
        return "d" + getRandomishId();
    }

    Map<String, String> createUserCreds(String db) {
        Map<String, String> userCredentials = new HashMap<>();

        String uid = createUserId();
        String pwd = createPassword();

        userCredentials.put(USERNAME, uid);
        userCredentials.put(PASSWORD, pwd);
        userCredentials.put(DATABASE, db);

        log.debug("creds: " + userCredentials.toString());
        jdbcTemplate.execute("USE [" + db + "]; CREATE USER [" + uid + "] WITH PASSWORD='" + pwd + "', DEFAULT_SCHEMA=[dbo]; EXEC sp_addrolemember 'db_owner', '" + uid + "'");

        log.info("Created user: " + userCredentials.get(USERNAME));
        return userCredentials;
    }

    void deleteUserCreds(String uid, String db) {
        jdbcTemplate.execute("use " + db + "; DROP USER IF EXISTS " + uid);
    }

    boolean checkUserExists(String uid, String db) {
        return jdbcTemplate.queryForObject("use " + db + "; SELECT count(name) FROM sys.database_principals WHERE name = ?", new Object[]{uid}, Integer.class) > 0;
    }
}