/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.pivotal.ecosystem.greenplum.broker.database;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.pivotal.ecosystem.greenplum.broker.util.Utils;

@Component
public class Database {
	private static final Logger logger = LoggerFactory.getLogger(Database.class);

    public void createDatabaseForInstance(String instanceId, String serviceId, String planId, String organizationGuid, String spaceGuid) throws SQLException {
        Utils.checkValidUUID(instanceId);
        GreenplumDatabase.executeUpdate("CREATE DATABASE \"" + instanceId + "\" ENCODING 'UTF8'");
        GreenplumDatabase.executeUpdate("REVOKE all on database \"" + instanceId + "\" from public");

        Map<Integer, String> parameterMap = new HashMap<Integer, String>();
        parameterMap.put(1, instanceId);
        parameterMap.put(2, serviceId);
        parameterMap.put(3, planId);
        parameterMap.put(4, organizationGuid);
        parameterMap.put(5, spaceGuid);

        GreenplumDatabase.executePreparedUpdate("INSERT INTO service (serviceinstanceid, servicedefinitionid, planid, organizationguid, spaceguid) VALUES (?, ?, ?, ?, ?)", parameterMap);
    }

    public void deleteDatabase(String instanceId) throws SQLException {
        Utils.checkValidUUID(instanceId);

        Map<Integer, String> parameterMap = new HashMap<Integer, String>();
        parameterMap.put(1, instanceId);

        Map<String, String> result = GreenplumDatabase.executeSelect("SELECT current_user");
        String currentUser = null;
        if(result != null) {
            currentUser = result.get("current_user");
        }
//      logger.debug("check user" + currentUser);

        if(currentUser == null) {
            logger.error("Current user for instance '" + instanceId + "' could not be found");
        }

        GreenplumDatabase.executePreparedSelect("SELECT pg_terminate_backend(pg_stat_activity.procpid) FROM pg_stat_activity WHERE pg_stat_activity.datname = ? AND procpid <> pg_backend_pid()", parameterMap);
        GreenplumDatabase.executeUpdate("ALTER DATABASE \"" + instanceId + "\" OWNER TO \"" + currentUser + "\"");
        GreenplumDatabase.executeUpdate("DROP DATABASE IF EXISTS \"" + instanceId + "\"");
        GreenplumDatabase.executePreparedUpdate("DELETE FROM service WHERE serviceinstanceid=?", parameterMap);
    }

}