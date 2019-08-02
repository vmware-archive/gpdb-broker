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
import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.security.InvalidParameterException;

import io.pivotal.ecosystem.greenplum.broker.util.Utils;


@Component
public class Role {
    private static final Logger logger = LoggerFactory.getLogger(GreenplumDatabase.class);
	
	@Autowired
	private GreenplumDatabase greenplum;

    @Autowired
	private Environment env;

    public void createRoleForInstance(String instanceId)
            throws SQLException
    {
        String adminUserProperty="spring.datasource.username";

        logger.info("in createRoleForInstance, instanceID = " + instanceId);

        Utils.checkValidUUID(instanceId);

        String adminUser = env.getProperty(adminUserProperty);
        if (adminUser == null) {
            throw new InvalidParameterException("Admin user property '" + adminUserProperty + "' not set");
        }

        greenplum.executeUpdate("CREATE ROLE \"" + instanceId + "\" LOGIN");
        greenplum.executeUpdate("GRANT \"" + instanceId + "\" TO \"" + adminUser + "\"");
        greenplum.executeUpdate("ALTER DATABASE \"" + instanceId + "\" OWNER TO \"" + instanceId + "\"");
    }

    public void deleteRole(String instanceId)
            throws SQLException
    {
        logger.info("in deleteRole, instanceID = " + instanceId);
        Utils.checkValidUUID(instanceId);
        greenplum.executeUpdate("DROP ROLE IF EXISTS \"" + instanceId + "\"");
    }

    public String bindRoleToDatabase(String dbInstanceId)
            throws SQLException
    {
        logger.info("in bindRoleToDatabase, dbInstanceID = " + dbInstanceId);

        Utils.checkValidUUID(dbInstanceId);

        SecureRandom random = new SecureRandom();
        String passwd = new BigInteger(130, random).toString(32);

        greenplum.executeUpdate("ALTER ROLE \"" + dbInstanceId + "\" LOGIN password '" + passwd + "'");
        return passwd;
    }

    public void unBindRoleFromDatabase(String dbInstanceId)
            throws SQLException
    {
        logger.info("in unBindRoleFromDatabase, dbInstanceID = " + dbInstanceId);
        Utils.checkValidUUID(dbInstanceId);
        greenplum.executeUpdate("ALTER ROLE \"" + dbInstanceId + "\" NOLOGIN");
    }
}
