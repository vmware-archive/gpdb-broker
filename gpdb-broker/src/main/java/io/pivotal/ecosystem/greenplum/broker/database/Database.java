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
import java.sql.Types;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.pivotal.ecosystem.greenplum.broker.util.Utils;

@Component
public class Database {
	private static final Logger logger = LoggerFactory.getLogger(Database.class);
    private String tableName = "gpbroker_service";

	@Autowired
	private GreenplumDatabase greenplum;
	
    public void createDatabaseForInstance(String instanceId, String serviceId, String planId, String organizationGuid, String spaceGuid) throws SQLException {
        Utils.checkValidUUID(instanceId);
        greenplum.executeUpdate("CREATE DATABASE \"" + instanceId + "\" ENCODING 'UTF8'");
        greenplum.executeUpdate("REVOKE ALL ON DATABASE \"" + instanceId + "\" FROM public");

        Object[] params = { instanceId, serviceId, planId, organizationGuid, spaceGuid};
        int[] types = {Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR};
    		
        greenplum.executePreparedUpdate("INSERT INTO " + tableName +
                   " (service_instance_id, service_definition_id, plan_id, organization_guid, space_guid) " +
				   " VALUES (?,?,?,?,?)", params, types);
    }

    public void deleteDatabase(String instanceId) throws SQLException {
        Utils.checkValidUUID(instanceId);

        Map<String, String> result = greenplum.executeSelect("SELECT current_user");
        String currentUser = null;
        if(result != null) {
            currentUser = result.get("current_user");
        }
//      logger.debug("check user" + currentUser);

        if(currentUser == null) {
            logger.error("Current user for instance '" + instanceId + "' could not be found");
        }

        greenplum.executeUpdate("SELECT pg_terminate_backend(pg_stat_activity.procpid) "
                                + " FROM pg_stat_activity WHERE pg_stat_activity.datname = '"
        		                + instanceId + "' AND procpid <> pg_backend_pid()");
        greenplum.executeUpdate("ALTER DATABASE \"" + instanceId + "\" OWNER TO \"" + currentUser + "\"");
        greenplum.executeUpdate("DROP DATABASE IF EXISTS \"" + instanceId + "\"");
        greenplum.executeUpdate("DELETE FROM " + tableName + " WHERE service_instance_id = '" + instanceId + "'");
    }
}
