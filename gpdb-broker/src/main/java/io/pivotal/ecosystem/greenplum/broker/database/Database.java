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
import java.util.List;
import java.security.InvalidParameterException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.pivotal.ecosystem.greenplum.broker.util.Utils;

@Component
public class Database {

	private static final Logger logger = LoggerFactory.getLogger(Database.class);
	private String tableName = "gpbroker_service";

	@Autowired
	private GreenplumDatabase greenplum;
	
	private static String adminUser = System.getenv("GPDB_USER");
	
	private static String adminDb = System.getenv("ADMIN_DB");
	
	public static String getAdminUser () {
		if (adminUser == null || adminUser.length() == 0) {
			throw new InvalidParameterException("GPDB_USER environment variable must be set in manifest.yml");
		}
		return adminUser;
	}
	
	public static String getAdminDb () {
		if (adminDb == null || adminDb.length() == 0) {
			throw new InvalidParameterException("ADMIN_DB environment variable must be set in manifest.yml");
		}
		return adminDb;
	}

	public void createDatabaseForInstance(String instanceId, String serviceId, String planId, String organizationGuid,
			String spaceGuid) throws SQLException {
		logger.info("in createDatabaseForInstance: " + instanceId);
		Utils.checkValidUUID(instanceId);
		greenplum.executeUpdate("CREATE DATABASE \"" + instanceId + "\" ENCODING 'UTF8'");
		greenplum.executeUpdate("REVOKE ALL ON DATABASE \"" + instanceId + "\" FROM public");

		Object[] params = { instanceId, serviceId, planId, organizationGuid, spaceGuid };
		int[] types = { Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR };

		greenplum.executePreparedUpdate("INSERT INTO " + tableName
				+ " (service_instance_id, service_definition_id, plan_id, organization_guid, space_guid) "
				+ " VALUES (?,?,?,?,?)", params, types);
	}

	public void disableDatabase(String instanceId) throws SQLException, InvalidParameterException {

		logger.info("in disableDatabase: " + instanceId);
		Utils.checkValidUUID(instanceId);

		// Ensure nobody can connect
		greenplum.executeUpdate("REVOKE CONNECT ON DATABASE \"" + instanceId + "\" FROM public;");
		greenplum.executeUpdate("ALTER DATABASE \"" + instanceId + "\" OWNER TO \"" + getAdminUser() + "\"");

		// Schedule the DB to be dropped later
		greenplum.executeUpdate(
				"UPDATE " + tableName + " SET disabled_at = now() WHERE service_instance_id = '" + instanceId + "'");
	}

	@Scheduled(fixedDelay = 60000)
	public void cronDropDisabledDBs() {
		logger.info("Running cronDropDisabledDBs ...");
		String query =
				"SELECT service_instance_id\n"
				+ "FROM " + tableName + "\n"
				+ "WHERE\n"
				+ "  disabled_at IS NOT NULL\n"
				+ "  AND dropped_at IS NULL\n"
				+ "  AND service_instance_id NOT IN (SELECT datname FROM pg_stat_activity)\n"
				+ "ORDER BY disabled_at ASC";
		List<String> dbToDropList = greenplum.getListFromSelect(query);
		for (String dbToDrop : dbToDropList) {
			logger.info("cronDropDisabledDBs: attempting to drop DB " + dbToDrop + " now");
			boolean status = true;
			String msg = null;
			try {
				greenplum.dropDatabase(dbToDrop);
				greenplum.executeUpdate("UPDATE " + tableName + " SET dropped_at = now() WHERE service_instance_id = '"
						+ dbToDrop + "'");
			} catch (Exception e) {
				status = false;
				msg = e.getMessage();
			}
			logger.info("cronDropDisabledDBs: " + (status ? "SUCCEEDED" : "FAILED"));
			if (msg != null) {
				logger.info("cronDropDisabledDBs: " + msg);
			}
		}
		if (dbToDropList.size() == 0) {
			logger.info("cronDropDisabledDBs: no eligible DBs");
		}
	}
}
