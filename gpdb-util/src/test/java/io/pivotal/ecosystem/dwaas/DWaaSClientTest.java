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

import io.pivotal.ecosystem.dwaas.DWaaSClient;
import io.pivotal.ecosystem.dwaas.connector.DWaaSServiceInfo;
import io.pivotal.ecosystem.servicebroker.model.ServiceBinding;
import io.pivotal.ecosystem.servicebroker.model.ServiceInstance;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public class DWaaSClientTest {
	
	private static final Logger log = LoggerFactory.getLogger(DWaaSClientTest.class);

    @Autowired
    private DWaaSClient client;

    @Autowired
    private ServiceBinding serviceBindingWithParms;

    @Autowired
    private ServiceBinding serviceBindingNoParms;

    @Autowired
    private ServiceInstance serviceInstanceWithParams;

    @Autowired
    private ServiceInstance serviceInstanceNoParams;

    /*
    @Test
    public void testCreateAndDeleteWithParms() throws SQLException {
        testCreateAndDeleteDatabase(serviceInstanceWithParams, serviceBindingWithParms);
    }

    @Test
    public void testCreateAndDeleteNoParms() throws SQLException {
        testCreateAndDeleteDatabase(serviceInstanceNoParams, serviceBindingNoParms);
    }
    */
    
    @Autowired
    DataSource dataSource;

    private void testCreateAndDeleteDatabase(ServiceInstance serviceInstance, ServiceBinding binding) throws Exception {
        String db = "gpadmin"; //client.createDatabase(serviceInstance);
        assertNotNull(db);
        binding.getParameters().put(DWaaSServiceInfo.DATABASE, db);

        Map<String, String> userCredentials = client.createUserCreds(binding);

        String uid = userCredentials.get(DWaaSServiceInfo.USERNAME);
        assertNotNull(uid);

        String pw = userCredentials.get(DWaaSServiceInfo.PASSWORD);
        assertNotNull(pw);

        assertEquals(db, userCredentials.get(DWaaSServiceInfo.DATABASE));

        //DataSource dataSource = datasource();
        //String url = client.getDbUrl();
        //dataSource.setURL(url);
        //dataSource.setUser(userCredentials.get(DWaaSServiceInfo.USERNAME));
        //dataSource.setPassword(userCredentials.get(DWaaSServiceInfo.PASSWORD));

        Connection c = dataSource.getConnection();
        assertNotNull(c);
        c.close();

        assertTrue(client.checkUserExists(uid));
        client.deleteUserCreds(uid);
        assertFalse(client.checkUserExists(uid));

        //client.deleteDatabase(db.toString());
        //assertFalse(client.checkDatabaseExists(db.toString()));
    }

    @Test
    public void testDbExists() {
        assertTrue(client.checkDatabaseExists("master"));
        assertFalse(client.checkDatabaseExists("kjfhskfjd"));
    }

    /*
    @Test
    public void testUri() {
        assertEquals("jdbc:sqlserver://35.188.63.27:1433", client.getDbUrl(null));
        assertEquals("jdbc:sqlserver://35.188.63.27:1433;databaseName=foo", client.getDbUrl("foo"));
    }
    */

    @Test
    public void testClean() {
        assertEquals("dsfuwe98fy2Yd9y2", client.clean("dsfuwe98fy2Yd9y2"));
        assertEquals("dsfuwe98fy2Yd9y2", client.clean("dsfuw  e98f& ()$%^$ <> y2Yd9y2"));
        assertEquals("", client.clean(""));
        assertEquals("", client.clean("        "));
        assertEquals("", client.clean(null));
        assertEquals("dfjhgkjhfdgtjhowefiTT", client.clean("dfjhgkjhfd<>&gt;// \\ jhowefi TT "));
    }
}