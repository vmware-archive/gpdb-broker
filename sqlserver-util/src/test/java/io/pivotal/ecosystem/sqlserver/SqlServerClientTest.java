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
import io.pivotal.ecosystem.sqlserver.connector.SqlServerServiceInfo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Connection;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestConfig.class)
@Slf4j
public class SqlServerClientTest {

    @Autowired
    private SqlServerClient client;

    @Test
    public void testCreateAndDeleteDatabase() throws Exception {
        String db = client.createDatabase();

        Map<String, String> userCredentials = client.createUserCreds(db);

        String uid = userCredentials.get(SqlServerServiceInfo.USERNAME);
        assertNotNull(uid);

        String pw = userCredentials.get(SqlServerServiceInfo.PASSWORD);
        assertNotNull(pw);

        assertEquals(db, userCredentials.get(SqlServerServiceInfo.DATABASE));

        SQLServerConnectionPoolDataSource dataSource = new SQLServerConnectionPoolDataSource();
        String url = client.getDbUrl(db);
        dataSource.setURL(url);
        dataSource.setUser(userCredentials.get(SqlServerServiceInfo.USERNAME));
        dataSource.setPassword(userCredentials.get(SqlServerServiceInfo.PASSWORD));

        Connection c = dataSource.getConnection();
        assertNotNull(c);
        c.close();

        assertTrue(client.checkUserExists(uid, db));
        client.deleteUserCreds(uid, db);
        assertFalse(client.checkUserExists(uid, db));

        client.deleteDatabase(db);
        assertFalse(client.checkDatabaseExists(db));
    }

    @Test
    public void testDbExists() {
        assertTrue(client.checkDatabaseExists("master"));
        assertFalse(client.checkDatabaseExists("kjfhskfjd"));
    }

    @Test
    public void testUri() {
        assertEquals("jdbc:sqlserver://35.188.63.27:1433", client.getDbUrl(null));
        assertEquals("jdbc:sqlserver://35.188.63.27:1433;databaseName=foo", client.getDbUrl("foo"));
    }
}