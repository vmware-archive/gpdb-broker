/**
 * Copyright (C) 2016-Present Pivotal Software, Inc. All rights reserved.
 * <p>
 * This program and the accompanying materials are made available under
 * the terms of the under the Apache License, Version 2.0 (the "License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.pivotal.ecosystem.sqlserver;

import io.pivotal.ecosystem.servicebroker.model.ServiceBinding;
import io.pivotal.ecosystem.servicebroker.model.ServiceInstance;
import io.pivotal.ecosystem.servicebroker.service.DefaultServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Example service broker. Can be used as a template for creating custom service brokers
 * by adding your code in the appropriate methods. For more information on the CF service broker
 * lifecycle and API, please see See <a href="https://docs.cloudfoundry.org/services/api.html">here.</a>
 * <p>
 * This class extends DefaultServiceImpl, which has no-op implementations of the methods. This means
 * that if, for instance, your broker does not support binding you can just delete the binding methods below
 * (in other words, you do not need to implement your own no-op implementations).
 */
@Service
@Slf4j
class SqlServerBroker extends DefaultServiceImpl {

    private Environment env;
    private SqlServerClient client;


    public SqlServerBroker(Environment env, SqlServerClient client) {
        super();
        this.env = env;
        this.client = client;
    }


    /**
     * Add code here and it will be run during the create-service process. This might include
     * calling back to your underlying service to create users, schemas, fire up environments, etc.
     *
     * @param instance service instance data passed in by the cloud connector. Clients can pass additional json
     *                 as part of the create-service request, which will show up as key value pairs in instance.parameters.
     */
    @Override
    public void createInstance(ServiceInstance instance) throws ServiceBrokerException {
        log.info("creating database...");
        String db = client.createDatabase();
        instance.getParameters().put(SqlServerClient.DATABASE, db);
        log.info("database: " + db + " created.");
    }

    /**
     * Code here will be called during the delete-service instance process. You can use this to de-allocate resources
     * on your underlying service, delete user accounts, destroy environments, etc.
     *
     * @param instance service instance data passed in by the cloud connector.
     */
    @Override
    public void deleteInstance(ServiceInstance instance) {
        String db = instance.getParameters().get(SqlServerClient.DATABASE).toString();
        log.info("deleting database: " + db);
        client.deleteDatabase(db);
    }

    /**
     * Code here will be called during the update-service process. You can use this to modify
     * your service instance.
     *
     * @param instance service instance data passed in by the cloud connector.
     */
    @Override
    public void updateInstance(ServiceInstance instance) {
        log.info("update not yet implemented");
    }

    /**
     * Called during the bind-service process. This is a good time to set up anything on your underlying service specifically
     * needed by an application, such as user accounts, rights and permissions, application-specific environments and connections, etc.
     * <p>
     * Services that do not support binding should set '"bindable": false,' within their catalog.json file. In this case this method
     * can be safely deleted in your implementation.
     *
     * @param instance service instance data passed in by the cloud connector.
     * @param binding  binding data passed in by the cloud connector. Clients can pass additional json
     *                 as part of the bind-service request, which will show up as key value pairs in binding.parameters. Brokers
     *                 can, as part of this method, store any information needed for credentials and unbinding operations as key/value
     *                 pairs in binding.properties
     */
    @Override
    public void createBinding(ServiceInstance instance, ServiceBinding binding) {
        String db = instance.getParameters().get(SqlServerClient.DATABASE).toString();
        log.info("binding app: " + binding.getAppGuid() + " to database: " + db);

        Map<String, String> userCredentials = client.createUserCreds(db);
        binding.getParameters().put(SqlServerClient.USERNAME, userCredentials.get(SqlServerClient.USERNAME));
        binding.getParameters().put(SqlServerClient.PASSWORD, userCredentials.get(SqlServerClient.PASSWORD));
        binding.getParameters().put(SqlServerClient.DATABASE, db);
    }

    /**
     * Called during the unbind-service process. This is a good time to destroy any resources, users, connections set up during the bind process.
     *
     * @param instance service instance data passed in by the cloud connector.
     * @param binding  binding data passed in by the cloud connector.
     */
    @Override
    public void deleteBinding(ServiceInstance instance, ServiceBinding binding) {
        log.info("unbinding app: " + binding.getAppGuid() + " from database: " + instance.getParameters().get(SqlServerClient.DATABASE));
        client.deleteUserCreds(binding.getParameters().get(SqlServerClient.USERNAME).toString(), binding.getParameters().get(SqlServerClient.DATABASE).toString());
    }

    /**
     * Bind credentials that will be returned as the result of a create-binding process. The format and values of these credentials will
     * depend on the nature of the underlying service. For more information and some examples, see
     * <a href=https://docs.cloudfoundry.org/services/binding-credentials.html>here.</a>
     * <p>
     * This method is called after the create-binding method: any information stored in binding.properties in the createBinding call
     * will be available here, along with any custom data passed in as json parameters as part of the create-binding process by the client.
     *
     * @param instance service instance data passed in by the cloud connector.
     * @param binding  binding data passed in by the cloud connector.
     * @return credentials, as a series of key/value pairs
     */
    @Override
    public Map<String, Object> getCredentials(ServiceInstance instance, ServiceBinding binding) {
        log.info("returning credentials.");

        Map<String, Object> m = new HashMap<>();
        m.put("hostname", env.getProperty("SQL_HOST"));
        m.put("port", env.getProperty("SQL_PORT"));
        m.put("uri", client.getDbUrl(binding.getParameters().get(SqlServerClient.DATABASE).toString()));

        m.put(SqlServerClient.USERNAME, binding.getParameters().get(SqlServerClient.USERNAME));
        m.put(SqlServerClient.PASSWORD, binding.getParameters().get(SqlServerClient.PASSWORD));
        m.put(SqlServerClient.DATABASE, binding.getParameters().get(SqlServerClient.DATABASE));

        //TODO add uid and pw into connection string?
        return m;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

}