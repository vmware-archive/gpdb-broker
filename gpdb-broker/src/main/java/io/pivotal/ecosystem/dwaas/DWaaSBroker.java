/*
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

package io.pivotal.ecosystem.dwaas;

import io.pivotal.ecosystem.dwaas.DWaaSClient;
import io.pivotal.ecosystem.dwaas.connector.DWaaSServiceInfo;
import io.pivotal.ecosystem.servicebroker.model.ServiceBinding;
import io.pivotal.ecosystem.servicebroker.model.ServiceInstance;
import io.pivotal.ecosystem.servicebroker.service.DefaultServiceImpl;
import lombok.extern.slf4j.Slf4j;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.context.annotation.ComponentScan;
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
class DWaaSBroker extends DefaultServiceImpl {

    @Autowired
    private DWaaSClient client;


    private Environment env;

    private static final Logger log = LoggerFactory.getLogger(DWaaSBroker.class);

    public DWaaSBroker(Environment env, DWaaSClient client) {
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

        //user can optionally specify a db name
        log.info("instance created.");
    }

    /**
     * Code here will be called during the delete-service instance process. You can use this to de-allocate resources
     * on your underlying service, delete user accounts, destroy environments, etc.
     *
     * @param instance service instance data passed in by the cloud connector.
     */
    @Override
    public void deleteInstance(ServiceInstance instance) {
        String db = instance.getParameters().get(DWaaSServiceInfo.DATABASE).toString();
        log.info("deleting database: " + db);
        //client.deleteDatabase(db);
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
        log.info("Binding a new Service Message For Testing");

        if (client == null) {
            log.info("CLIENT IS NULL***************");
            return;
        } else {
            try {
                Map<String, String> userCredentials = client.createUserCreds(binding);
                log.info("USER CREDENTIALS CREATED");
                binding.getParameters().put(DWaaSServiceInfo.USERNAME, userCredentials.get(DWaaSServiceInfo.USERNAME));
                binding.getParameters().put(DWaaSServiceInfo.PASSWORD, userCredentials.get(DWaaSServiceInfo.PASSWORD));
                binding.getParameters().put(DWaaSServiceInfo.DATABASE, userCredentials.get(DWaaSServiceInfo.DATABASE));
            } catch (Exception e) {
                log.error("BindingException: {}",e.getMessage());
            }
        }
        log.info("bound app: " + binding.getAppGuid() + " to database: " + "gpadmin");
    }

    /**
     * Called during the unbind-service process. This is a good time to destroy any resources, users, connections set up during the bind process.
     *
     * @param instance service instance data passed in by the cloud connector.
     * @param binding  binding data passed in by the cloud connector.
     */
    @Override
    public void deleteBinding(ServiceInstance instance, ServiceBinding binding) {
        log.info("DELETE Binding ");
        Map<String, Object> userCredentials = getCredentials(instance, binding);
        client.deleteUserCreds(userCredentials);
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
        String theUser = (String) binding.getParameters().get(DWaaSServiceInfo.USERNAME);
        log.info("User,{}", theUser);
        String thePasswd = (String) binding.getParameters().get(DWaaSServiceInfo.PASSWORD);
        log.info("Password {}", thePasswd);
        String theDB = (String) binding.getParameters().get(DWaaSServiceInfo.DATABASE);
        log.info("theDB {}", theDB);
        String uri = client.getDbUrl(theUser, theDB, thePasswd);
        log.info("URI {}", uri);

        Map<String, Object> m = new HashMap<>();
        m.put(DWaaSServiceInfo.URI, uri);

        m.put(DWaaSServiceInfo.USERNAME, theUser);
        m.put(DWaaSServiceInfo.PASSWORD, thePasswd);
        //m.put(DWaaSServiceInfo.DATABASE, binding.getParameters().get(DWaaSServiceInfo.DATABASE));
        //m.put(DWaaSServiceInfo.DATABASE, instance.getParameters().get(DWaaSServiceInfo.DATABASE));

        return m;
    }

    @Override
    public boolean isAsync() {
        return false;
    }
}