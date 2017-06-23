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

import io.pivotal.ecosystem.servicebroker.model.ServiceBinding;
import io.pivotal.ecosystem.servicebroker.model.ServiceInstance;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;

import static io.pivotal.ecosystem.servicebroker.PostgresClient.*;

@Configuration
@PropertySource("classpath:application.properties")
class TestConfig {

    @Bean
    public String dbUrl(Environment env) {
        return POSTGRES_URI_SCHEME + "://" + env.getProperty(POSTGRES_HOST_KEY) + ":" + Integer.parseInt(env.getProperty(POSTGRES_PORT_KEY));
    }

    @Bean
    public ServiceBinding serviceBindingWithParms() {
        Map<String, Object> params = new HashMap<>();

        long l = System.currentTimeMillis();

        params.put(POSTGRES_DB, "testDb" + l);
        params.put(POSTGRES_USER, "testUser" + l);
        params.put(POSTGRES_PASSWORD, "testPassw0rd");
        CreateServiceInstanceBindingRequest request = new CreateServiceInstanceBindingRequest(null, null, null, null, params);
        return new ServiceBinding(request);
    }

    @Bean
    public ServiceBinding serviceBindingNoParms() {
        return new ServiceBinding(new CreateServiceInstanceBindingRequest());
    }

    @Bean
    public ServiceInstance serviceInstanceWithParams() {
        Map<String, Object> params = new HashMap<>();

        long l = System.currentTimeMillis();
        params.put(POSTGRES_DB, "testDb" + l);
        return new ServiceInstance(new CreateServiceInstanceRequest(null, null, null, null, params));
    }

    @Bean
    public ServiceInstance serviceInstanceNoParams() {
        return new ServiceInstance(new CreateServiceInstanceRequest());
    }
}