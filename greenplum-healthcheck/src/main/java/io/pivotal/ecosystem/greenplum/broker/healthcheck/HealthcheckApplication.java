package io.pivotal.ecosystem.greenplum.broker.healthcheck;

import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.services.BindServiceInstanceRequest;
import org.cloudfoundry.operations.services.CreateServiceInstanceRequest;
import org.cloudfoundry.operations.services.DeleteServiceInstanceRequest;
import org.cloudfoundry.operations.services.UnbindServiceInstanceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.pivotal.ecosystem.greenplum.broker.healthcheck.config.EmailService;
import reactor.core.publisher.Mono;

@SpringBootApplication
@EnableScheduling
public class HealthcheckApplication {
	public static final Logger logger = LoggerFactory.getLogger(HealthcheckApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(HealthcheckApplication.class, args);
	}

	@Autowired
	EmailService emailService;

	@Component
	public class ScheduledTasks {
		@Autowired
		private EmailService emailService;
		@Autowired
		DefaultCloudFoundryOperations defaultCloudFoundryOperations;

		@Scheduled(fixedRate = 600000)
		public void createBindUndindDeleteServiceInstance() {
			logger.info("Running healthcheck");
			CreateServiceInstanceRequest createServiceInstanceRequest = CreateServiceInstanceRequest.builder()
					.serviceName("Greenplum").serviceInstanceName("test-gpdb-scheduler-healthcheck-service")
					.planName("Free").build();

			BindServiceInstanceRequest bindServiceInstanceRequest = BindServiceInstanceRequest.builder()
					.serviceInstanceName("test-gpdb-scheduler-healthcheck-service").applicationName("gpdb-test-client")
					.build();
			UnbindServiceInstanceRequest unbindServiceInstanceRequest = UnbindServiceInstanceRequest.builder()
					.serviceInstanceName("test-gpdb-scheduler-healthcheck-service").applicationName("gpdb-test-client")
					.build();
			DeleteServiceInstanceRequest deleteServiceInstanceRequest = DeleteServiceInstanceRequest.builder()
					.name("test-gpdb-scheduler-healthcheck-service").build();
			// Unbind and remove any leftover test-gpdb-scheduler-healthcheck-service instance
			defaultCloudFoundryOperations.services().unbind(unbindServiceInstanceRequest)
					.onErrorResume(throwable -> Mono.empty()).block();

			defaultCloudFoundryOperations.services().deleteInstance(deleteServiceInstanceRequest)
					.onErrorResume(throwable -> Mono.empty()).block();
			
			// test create, bind,unbind and delete of service instances
			defaultCloudFoundryOperations.services().createInstance(createServiceInstanceRequest)
					.then(defaultCloudFoundryOperations.services().bind(bindServiceInstanceRequest))
					.then(defaultCloudFoundryOperations.services().unbind(unbindServiceInstanceRequest))
					.then(defaultCloudFoundryOperations.services().deleteInstance(deleteServiceInstanceRequest))
					.doOnSuccess(t->logger.info("Healthcheck Success"))
					.doOnError(t -> emailService.accept(t.getMessage())).repeat(3).blockLast();
			logger.info("Healthcheck complete");
		}
	}
}
