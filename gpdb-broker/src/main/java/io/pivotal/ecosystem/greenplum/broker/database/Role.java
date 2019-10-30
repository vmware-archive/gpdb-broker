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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import io.pivotal.ecosystem.greenplum.broker.util.Utils;

import java.sql.SQLException;
import java.security.InvalidParameterException;

@Component
public class Role {
	private static final Logger logger = LoggerFactory.getLogger(GreenplumDatabase.class);

	@Autowired
	private GreenplumDatabase greenplum;

	@Autowired
	private Environment env;

	public void createRoleForInstance(String instanceId) throws SQLException {
		String adminUserProperty = "spring.datasource.username";

		logger.info("in createRoleForInstance, instanceID = " + instanceId);

		Utils.checkValidUUID(instanceId);

		String adminUser = env.getProperty(adminUserProperty);
		if (adminUser == null) {
			throw new InvalidParameterException("Admin user property '" + adminUserProperty + "' not set");
		}
		greenplum.executeUpdate("CREATE ROLE \"" + instanceId + "\""); // No LOGIN for this role
		greenplum.executeUpdate("ALTER DATABASE \"" + instanceId + "\" OWNER TO \"" + instanceId + "\"");
		greenplum.executeUpdate("GRANT ALL ON SCHEMA public TO \"" + instanceId + "\"");
	}
	
	public void deleteRole(String db, String role) throws SQLException {
		logger.info("in deleteRole(db = " + db + ", role = " + role + ")");
		Utils.checkValidUUID(role);
		String sql = "DROP OWNED BY \"" + role + "\" CASCADE;";
		sql += "DROP ROLE IF EXISTS \"" + role + "\"";
		greenplum.executeUpdateForDb(db, sql);
	}

	public void deleteRole(String role) throws SQLException {
		logger.info("in deleteRole, role = " + role);
		Utils.checkValidUUID(role);
		greenplum.executeUpdate("DROP OWNED BY \"" + role + "\"");
		greenplum.executeUpdate("DROP ROLE IF EXISTS \"" + role + "\"");
	}

	/* 
	 * New approach: the owner role isn't actually used for connections, but each binding
	 * causes creation of new [role, passwd] here, where this new role is granted the role
	 * which owns the DB.  A `cf create-service-key ...` looks just like `cf bind-service ...`,
	 * so this seems like the best way.
	 */
	public String createAndBindRole(String instanceId, String role) throws SQLException {
		logger.info("in createAndBindRole, role = " + role);
		Utils.checkValidUUID(role);
		String passwd = Utils.genRandPasswd();
		//greenplum.executeUpdate("create role \"" + role + "\" in role \"" + instanceId + "\" login password '" + passwd + "'");
		greenplum.executeUpdate("CREATE ROLE \"" + role + "\"");
		greenplum.executeUpdate("GRANT \"" + instanceId + "\" TO \"" + role + "\"");
		greenplum.executeUpdate("ALTER ROLE \"" + role + "\" LOGIN password '" + passwd + "'");
		return passwd;
	}

}
