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
import org.springframework.stereotype.Component;

import io.pivotal.ecosystem.greenplum.broker.util.Utils;

import java.sql.SQLException;

@Component
public class Role {
	private static final Logger logger = LoggerFactory.getLogger(GreenplumDatabase.class);

	@Autowired
	private GreenplumDatabase greenplum;

	public void createRoleForInstance(String instanceId) throws SQLException {

		logger.info("in createRoleForInstance, instanceID = " + instanceId);

		Utils.checkValidUUID(instanceId);

		greenplum.executeUpdate("CREATE ROLE \"" + instanceId + "\" LOGIN");
		greenplum.executeUpdate("ALTER DATABASE \"" + instanceId + "\" OWNER TO \"" + instanceId + "\"");
		//greenplum.executeUpdate("ALTER DATABASE \"" + instanceId + "\" SET ROLE \"" + instanceId + "\"");
		greenplum.executeUpdate("GRANT ALL ON SCHEMA public TO \"" + instanceId + "\"");
	}
	
	private void dropOwnedBy (String db, String role) throws SQLException {
		logger.info("in dropOwnedBy(db = " + db + ", role = " + role + ")");
		String sql = "DROP OWNED BY \"" + role + "\" CASCADE;";
		greenplum.executeUpdateForDb(db, sql);
		greenplum.executeUpdateForDb(Database.getAdminDb(), sql); // Also drop anything in admin DB (e.g. gpadmin DB)
	}
	
	public void deleteRole(String db, String role) throws SQLException {
		logger.info("in deleteRole(db = " + db + ", role = " + role + ")");
		Utils.checkValidUUID(role);
		dropOwnedBy(db, role);
		String sql = "DROP ROLE IF EXISTS \"" + role + "\"";
		greenplum.executeUpdateForDb(db, sql);
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
		greenplum.executeUpdate("CREATE ROLE \"" + role + "\" LOGIN");
		greenplum.executeUpdate("GRANT \"" + instanceId + "\" TO \"" + role + "\"");
		greenplum.executeUpdate("ALTER ROLE \"" + role + "\" password '" + passwd + "'");
		return passwd;
	}

}
