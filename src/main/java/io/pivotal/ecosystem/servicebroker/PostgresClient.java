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
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Repository
@Slf4j
class PostgresClient {

    private JdbcTemplate jdbcTemplate;

    static final String POSTGRES_DB = "POSTGRES_DB";
    static final String POSTGRES_USER = "POSTGRES_USER";
    static final String POSTGRES_PASSWORD = "POSTGRES_PASSWORD";
    static final String POSTGRES_HOST_KEY = "POSTGRES_HOST";
    static final String POSTGRES_PORT_KEY = "POSTGRES_PORT";


    static final String POSTGRES_URI = "postgresuri";
    static final String POSTGRES_URI_SCHEME = "jdbc:postgresql";


    PostgresClient(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    String createDatabase(ServiceInstance instance) {
        String db = createDbName(instance.getParameters().get(POSTGRES_DB));
        jdbcTemplate.execute("CREATE DATABASE " + db);
        log.info("Database: " + db + " created successfully...");
        return db;
    }

    void deleteDatabase(String db) {
        jdbcTemplate.execute("DROP DATABASE IF EXISTS " + db);
        log.info("Database: " + db + " deleted successfully...");
    }

    boolean checkDatabaseExists(String db) {

        return jdbcTemplate.queryForObject("SELECT count(*) from pg_database WHERE datname = ?", new Object[]{db}, Integer.class) > 0;

    }

    //todo how to protect dbs etc. from bad actors?
    private String getRandomishId() {
        return clean(UUID.randomUUID().toString());
    }

    /**
     * jdbcTemplate helps protect against sql injection, but also clean strings up just in case
     */
    private String clean(String s) {
        if (s == null) {
            return "";
        }
        return s.replaceAll("[^a-zA-Z0-9]", "");
    }

    private String checkString(String s) throws ServiceBrokerException {
        if (s.equals(clean(s))) {
            return s;
        }
        throw new ServiceBrokerException("Name must contain only alphanumeric characters.");
    }

    private String createUserId(Object o) {
        if (o != null) {
            return checkString(o.toString());
        }
        return "u" + getRandomishId();
    }

    private String createPassword(Object o) {
        if (o != null) {
            return checkString(o.toString());
        }
        return "P" + getRandomishId();
    }

    private String createDbName(Object o) {
        if (o != null) {
            return checkString(o.toString());
        }
        return "d" + getRandomishId();
    }

    Map<String, String> createUserCreds(ServiceBinding binding) {
        String db = binding.getParameters().get(POSTGRES_DB).toString();
        Map<String, String> userCredentials = new HashMap<>();

        //users can optionally pass in uids and passwords
        userCredentials.put(POSTGRES_USER, createUserId(binding.getParameters().get(POSTGRES_USER)));
        userCredentials.put(POSTGRES_PASSWORD, createPassword(binding.getParameters().get(POSTGRES_PASSWORD)));
        userCredentials.put(POSTGRES_DB, db);
        log.debug("creds: " + userCredentials.toString());

        jdbcTemplate.execute("CREATE USER " + userCredentials.get(POSTGRES_USER) + " WITH PASSWORD '" + userCredentials.get(POSTGRES_PASSWORD) + "'");
        jdbcTemplate.execute("GRANT ALL PRIVILEGES ON DATABASE " + userCredentials.get(POSTGRES_DB) + " to " + userCredentials.get(POSTGRES_USER));

        log.info("Created user: " + userCredentials.get(POSTGRES_USER));
        return userCredentials;
    }

    void deleteUserCreds(String uid) {
        jdbcTemplate.execute("DROP USER IF EXISTS " + uid);
    }

}