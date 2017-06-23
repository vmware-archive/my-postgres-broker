/*
 * Copyright (C) 2017-Present Pivotal Software, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the under the Apache License, Version 2.0 (the "License”);
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.pivotal.ecosystem.servicebroker;

import io.pivotal.ecosystem.servicebroker.model.LastOperation;
import io.pivotal.ecosystem.servicebroker.model.ServiceBinding;
import io.pivotal.ecosystem.servicebroker.model.ServiceInstance;
import io.pivotal.ecosystem.servicebroker.service.DefaultServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static io.pivotal.ecosystem.servicebroker.PostgresClient.*;

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
class PostgresBroker extends DefaultServiceImpl {

    private PostgresClient client;

    private String dbUrl;


    public PostgresBroker(PostgresClient client, String dbUrl) {
        super();
        this.client = client;
        this.dbUrl = dbUrl;
    }

    /**
     * Add code here and it will be run during the create-service process. This might include
     * calling back to your underlying service to create users, schemas, fire up environments, etc.
     *
     * @param instance service instance data passed in by the cloud connector. Clients can pass additional json
     *                 as part of the create-service request, which will show up as key value pairs in instance.parameters.
     */
    @Override
    public LastOperation createInstance(ServiceInstance instance) {
        log.info("creating database...");

        try {
            String db = client.createDatabase(instance);
            log.info("database: " + db + " created.");
            instance.getParameters().put(POSTGRES_DB, db);
        } catch (Throwable t) {
            log.error("error creating database.", t);
            return new LastOperation(LastOperation.CREATE, LastOperation.FAILED, t.getMessage());
        }

        return new LastOperation(LastOperation.CREATE, LastOperation.SUCCEEDED, instance.getId() + " creating.");
    }

    /**
     * Code here will be called during the delete-service instance process. You can use this to de-allocate resources
     * on your underlying service, delete user accounts, destroy environments, etc.
     *
     * @param instance service instance data passed in by the cloud connector.
     */
    @Override
    public LastOperation deleteInstance(ServiceInstance instance) {
        try {
            String db = instance.getParameters().get(POSTGRES_DB).toString();
            String user = instance.getParameters().get(POSTGRES_USER).toString();
            log.info("deleting database: " + db);
            client.deleteDatabase(db);
            log.info("********DELETED database: " + db);
            client.deleteUserCreds(user);
            log.info("********DELETED User creds: " + user);
        } catch (Throwable t) {
            log.error("error deleting database.", t);
            return new LastOperation(LastOperation.DELETE, LastOperation.FAILED, t.getMessage());
        }
        return new LastOperation(LastOperation.DELETE, LastOperation.SUCCEEDED, instance.getId() + " deleting.");
    }

    /**
     * Code here will be called during the update-service process. You can use this to modify
     * your service instance.
     *
     * @param instance service instance data passed in by the cloud connector.
     */
    @Override
    public LastOperation updateInstance(ServiceInstance instance) {
        log.info("update not yet implemented");
        return new LastOperation(LastOperation.DELETE, LastOperation.FAILED, instance.getId() + " updating.");
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
    public LastOperation createBinding(ServiceInstance instance, ServiceBinding binding) {
        String db = instance.getParameters().get(POSTGRES_DB).toString();
        binding.getParameters().put(POSTGRES_DB, db);

        Map<String, String> userCredentials = client.createUserCreds(binding);
        binding.getParameters().put(POSTGRES_USER, userCredentials.get(POSTGRES_USER));

        binding.getParameters().put(POSTGRES_PASSWORD, userCredentials.get(POSTGRES_PASSWORD));
        log.info("bound app: " + binding.getAppGuid() + " to database: " + db);
        return new LastOperation(LastOperation.BIND, LastOperation.SUCCEEDED, "bound.");
    }

    /**
     * Called during the unbind-service process. This is a good time to destroy any resources, users, connections set up during the bind process.
     *
     * @param instance service instance data passed in by the cloud connector.
     * @param binding  binding data passed in by the cloud connector.
     */
    @Override
    public LastOperation deleteBinding(ServiceInstance instance, ServiceBinding binding) {
        log.info("unbinding app: " + binding.getAppGuid() + " from database: " + instance.getParameters().get(POSTGRES_DB));
        return new LastOperation(LastOperation.UNBIND, LastOperation.SUCCEEDED, "bound.");
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
        m.put(POSTGRES_URI, dbUrl + "/" + binding.getParameters().get(POSTGRES_DB).toString());

        m.put(POSTGRES_USER, binding.getParameters().get(POSTGRES_USER));
        m.put(POSTGRES_PASSWORD, binding.getParameters().get(POSTGRES_PASSWORD));
        m.put(POSTGRES_DB, binding.getParameters().get(POSTGRES_DB));

        return m;
    }

    @Override
    public boolean isAsync() {
        return false;
    }
}