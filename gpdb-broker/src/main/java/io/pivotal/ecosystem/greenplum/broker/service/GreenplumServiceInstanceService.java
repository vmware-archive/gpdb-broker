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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceResponse;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceResponse;
import org.springframework.cloud.servicebroker.model.GetLastServiceOperationRequest;
import org.springframework.cloud.servicebroker.model.GetLastServiceOperationResponse;
import org.springframework.cloud.servicebroker.model.OperationState;
import org.springframework.cloud.servicebroker.model.UpdateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.UpdateServiceInstanceResponse;
import org.springframework.cloud.servicebroker.service.ServiceInstanceService;
import org.springframework.stereotype.Service;

import io.pivotal.ecosystem.greenplum.broker.database.Database;
import io.pivotal.ecosystem.greenplum.broker.database.Role;

/*
 * - Service instance create
 * - Service instance delete
 */

@Service
public class GreenplumServiceInstanceService implements ServiceInstanceService {

	private static final Logger logger = LoggerFactory.getLogger(GreenplumServiceInstanceService.class);

	private final Database db;
	private final Role role;

	@Autowired
	public GreenplumServiceInstanceService(Database db, Role role) {
		this.db = db;
		this.role = role;
	}

	@Override
	public CreateServiceInstanceResponse createServiceInstance(
			CreateServiceInstanceRequest csir) {
		String serviceInstanceId = csir.getServiceInstanceId();
		String serviceId = csir.getServiceDefinitionId();
		String planId = csir.getPlanId();
		String organizationGuid = csir.getOrganizationGuid();
		String spaceGuid = csir.getSpaceGuid();
		try {
			db.createDatabaseForInstance(serviceInstanceId, serviceId, planId, organizationGuid, spaceGuid);
			role.createRoleForInstance(serviceInstanceId);
		} catch (SQLException e) {
			logger.error("Error while creating service instance '" + serviceInstanceId + "'", e);
			throw new ServiceBrokerException(e.getMessage());
		}
		return new CreateServiceInstanceResponse().withDashboardUrl("");
	}

	@Override
	public DeleteServiceInstanceResponse deleteServiceInstance(
			DeleteServiceInstanceRequest dsir) {
		String serviceInstanceId = dsir.getServiceInstanceId();

		try {
			db.disableDatabase(serviceInstanceId);
			role.deleteRole(serviceInstanceId, serviceInstanceId);
		} catch (SQLException e) {
			logger.error("Error while deleting service instance '" + serviceInstanceId + "'", e);
			throw new ServiceBrokerException(e.getMessage());
		}
		return new DeleteServiceInstanceResponse();
	}

	@Override
	public GetLastServiceOperationResponse getLastOperation(GetLastServiceOperationRequest arg0) {
		return new GetLastServiceOperationResponse().withOperationState(OperationState.SUCCEEDED);
	}

	@Override
	public UpdateServiceInstanceResponse updateServiceInstance(UpdateServiceInstanceRequest arg0) {
		throw new IllegalStateException("Not implemented");
	}

}
