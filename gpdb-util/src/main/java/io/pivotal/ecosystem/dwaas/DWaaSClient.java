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

package io.pivotal.ecosystem.dwaas;

import com.sun.javafx.font.directwrite.DWFactory;
import io.pivotal.ecosystem.dwaas.connector.DWaaSServiceInfo;

import io.pivotal.ecosystem.servicebroker.model.ServiceBinding;
import io.pivotal.ecosystem.servicebroker.model.ServiceInstance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Repository
class DWaaSClient {
    private static final Logger log = LoggerFactory.getLogger(DWaaSClient.class);
    private JdbcTemplate jdbcTemplate;
    private String url;

 /*  public DWaaSClient() {
    }*/

/*    DWaaSClient(DataSource dataSource, String dbUrl) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.url = dbUrl;
    }*/

    public DWaaSClient(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }


    boolean checkDatabaseExists(String db) {
        return jdbcTemplate.queryForObject("SELECT count(*) FROM pg_database WHERE datname = ?", new Object[]{db}, Integer.class) > 0;
    }

    String getDbUrl() {
        return this.url;
    }

    //todo how to protect dbs etc. from bad actors?
    private String getRandomishId() {
        return clean(UUID.randomUUID().toString());
    }

    /**
     * jdbcTemplate helps protect against sql injection, but also clean strings up just in case
     */
    String clean(String s) {
        if (s == null) {
            return "";
        }
        return s.replaceAll("[^a-zA-Z0-9]", "");
    }

    private String checkString(String s) throws ServiceBrokerException {
        if (s.equals(clean(s))) {
            return s;
        }
        throw new ServiceBrokerException("Name must contain only alphanumeric characters.");
    }

    private String createUserId(Object o) {
        if (o != null) {
            return checkString(o.toString());
        }
        return "u" + getRandomishId();
    }

    private String createPassword(Object o) {
        if (o != null) {
            return checkString(o.toString());
        }
        return "P" + getRandomishId();
    }

    Map<String, String> createUserCreds(ServiceBinding binding) {
        // String db = binding.getParameters().get(DWaaSServiceInfo.DATABASE).toString();
        Map<String, String> userCredentials = new HashMap<>();

        //users can optionally pass in uids and passwords
        userCredentials.put(DWaaSServiceInfo.USERNAME, createUserId(null)); //createUserId(binding.getParameters().get(USERNAME)));
        userCredentials.put(DWaaSServiceInfo.PASSWORD, createPassword(null)); //"createPassword(binding.getParameters().get(PASSWORD)));
        userCredentials.put(DWaaSServiceInfo.DATABASE, "database"); //db);
        log.debug("creds: " + userCredentials.toString());

        jdbcTemplate.execute("CREATE ROLE "
                + userCredentials.get(DWaaSServiceInfo.USERNAME)
                + " LOGIN SUPERUSER PASSWORD '"
                + userCredentials.get(DWaaSServiceInfo.PASSWORD)
                + "'");

        log.info("Created user: " + userCredentials.get(DWaaSServiceInfo.USERNAME));
        return userCredentials;
    }

    void deleteUserCreds(String uid) {
        if(uid=="gpadmin"){
            log.warn("CANNOT DELETE GPADMIN FROM GREENPLUM!!!");
            return;
        }
        jdbcTemplate.execute("DROP ROLE IF EXISTS " + uid);
    }

    boolean checkUserExists(String uid) {
        return jdbcTemplate.queryForObject("select count(*) from pg_roles where rolname = '?'", new Object[]{uid}, Integer.class) > 0;
    }
}