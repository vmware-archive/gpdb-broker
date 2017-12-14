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

		try {
			passwd = this.role.bindRoleToDatabase(serviceInstanceId);
		} catch (SQLException e) {
			logger.error("Error while creating service instance binding '" + bindingId + "'", e);
			throw new ServiceBrokerException(e.getMessage());
		}
		
		String uri = String.format("pivotal:greenplum://%s:%s@%s:%d/%s", serviceInstanceId, passwd, GreenplumDatabase.getDatabaseHost(),
				GreenplumDatabase.getDatabasePort(), serviceInstanceId);

		
		String jdbcURL = String.format("jdbc:pivotal:greenplum://%s:%d/;DatabaseName=%s", GreenplumDatabase.getDatabaseHost(),
				GreenplumDatabase.getDatabasePort(), serviceInstanceId);

		Map<String, Object> credentials = new HashMap<String, Object>();
		credentials.put("uri", uri);
		credentials.put("jdbcUrl", jdbcURL);
		credentials.put("username", serviceInstanceId);
		credentials.put("password", passwd);

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
