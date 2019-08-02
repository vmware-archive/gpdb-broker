/*
 * Copyright (C) 2016-Present Pivotal Software, Inc. All rights reserved.
 * This program and the accompanying materials are made available under
 * the terms of the under the Apache License, Version 2.0 (the "License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package io.pivotal.ecosystem.greenplum.broker.service;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingResponse;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.service.ServiceInstanceBindingService;
import org.springframework.stereotype.Service;

import io.pivotal.ecosystem.greenplum.broker.database.GreenplumDatabase;
import io.pivotal.ecosystem.greenplum.broker.database.Role;

@Service
public class GreenplumServiceInstanceBindingService implements ServiceInstanceBindingService {

	private static final Logger logger = LoggerFactory.getLogger(GreenplumServiceInstanceBindingService.class);
	private final Role role;

    @Autowired
    private GreenplumDatabase greenplum;
	
    @Autowired
    public GreenplumServiceInstanceBindingService(Role role) {
        this.role = role;
    }

    @Override
    public CreateServiceInstanceBindingResponse createServiceInstanceBinding(
            CreateServiceInstanceBindingRequest createServiceInstanceBindingRequest) {

        String bindingId = createServiceInstanceBindingRequest.getBindingId();
        String serviceInstanceId = createServiceInstanceBindingRequest.getServiceInstanceId();
        String appGuid = createServiceInstanceBindingRequest.getBoundAppGuid();
        String passwd = "";
        logger.info("in CreateServiceInstanceBindingResponse: ServiceInstanceId '" + serviceInstanceId + "', " + "appGuid '" + appGuid + "'");

        try {
            passwd = this.role.bindRoleToDatabase(serviceInstanceId);
        } catch (SQLException e) {
            logger.error("Error while creating service instance binding '" + bindingId + "'", e);
            throw new ServiceBrokerException(e.getMessage());
        }

//		Settings when using the Postgres JDBC driver
        String uri = String.format("postgres://%s:%s@%s:%d/%s",
                serviceInstanceId, passwd,
                greenplum.getDatabaseHost(), greenplum.getDatabasePort(),
                serviceInstanceId);
        String jdbcURL = String.format("jdbc:postgresql://%s:%d/%s?user=%s&password=%s&%s",
                greenplum.getDatabaseHost(), greenplum.getDatabasePort(),
                serviceInstanceId,  // database
                serviceInstanceId,  // user
                passwd,             // user password
                "sslmode=require");
		
        Map<String, Object> credentials = new HashMap<String, Object>();
        credentials.put("uri", uri);
        credentials.put("max-conns", 5);
        credentials.put("jdbcUrl", jdbcURL);
		
/*
 CraigS: Removed the entries below since it was causing clients to fail. The log message received was:
 Caused by: org.postgresql.util.PSQLException: The server requested password-based authentication, but no password was provided.
 Found this out when comparing what the PWS services for Postgres provided in the client env vs what this broker was providing.

		credentials.put("master host", gpdb.getDatabaseHost());
		credentials.put("master port", gpdb.getDatabasePort());
		credentials.put("username", serviceInstanceId);
		credentials.put("password", passwd);
*/
        logger.info("credentials '" + credentials + "");

        return new CreateServiceInstanceAppBindingResponse().withCredentials(credentials);
	}

	@Override
	public void deleteServiceInstanceBinding(DeleteServiceInstanceBindingRequest deleteServiceInstanceBindingRequest) {
		String serviceInstanceId = deleteServiceInstanceBindingRequest.getServiceInstanceId();
		String bindingId = deleteServiceInstanceBindingRequest.getBindingId();
		try {
			this.role.unBindRoleFromDatabase(serviceInstanceId);
		} catch (SQLException e) {
			logger.error("Error while deleting service instance binding '" + bindingId + "'", e);
			throw new ServiceBrokerException(e.getMessage());
		}

	}
}
