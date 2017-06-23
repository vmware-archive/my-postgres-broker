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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import static io.pivotal.ecosystem.servicebroker.PostgresClient.*;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestConfig.class)
@Slf4j
@Ignore
public class PostgressClientTest {

    @Autowired
    private PostgresClient client;

    @Autowired
    private ServiceBinding serviceBindingWithParms;

    @Autowired
    private ServiceBinding serviceBindingNoParms;

    @Autowired
    private ServiceInstance serviceInstanceWithParams;

    @Autowired
    private ServiceInstance serviceInstanceNoParams;

    @Autowired
    private DataSource dataSource;

    @Test
    public void testCreateAndDeleteWithParms() throws SQLException {
        testCreateAndDeleteDatabase(serviceInstanceWithParams, serviceBindingWithParms);
    }

    @Test
    public void testCreateAndDeleteNoParms() throws SQLException {
        testCreateAndDeleteDatabase(serviceInstanceNoParams, serviceBindingNoParms);
    }

    private void testCreateAndDeleteDatabase(ServiceInstance serviceInstance, ServiceBinding binding) throws SQLException {
        //REVOKE CONNECT ON DATABASE TARGET_DB FROM public;
        String db = client.createDatabase(serviceInstance);
        assertNotNull(db);
        binding.getParameters().put(POSTGRES_DB, db);

        Map<String, String> userCredentials = client.createUserCreds(binding);

        String uid = userCredentials.get(POSTGRES_USER);
        assertNotNull(uid);

        String pw = userCredentials.get(POSTGRES_PASSWORD);
        assertNotNull(pw);

        assertEquals(db, userCredentials.get(POSTGRES_DB));

        client.deleteDatabase(db);
        assertFalse(client.checkDatabaseExists(db));

        client.deleteUserCreds(uid);

    }

    @Test
    //@Ignore
    public void killConnections() throws Exception {

        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            Statement stmt = conn.createStatement();

            stmt.execute("SELECT pg_terminate_backend(pg_stat_activity.pid) FROM pg_stat_activity WHERE datname = current_database() AND pid <> pg_backend_pid()");

        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
}