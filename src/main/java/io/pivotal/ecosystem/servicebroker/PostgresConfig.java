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

import org.postgresql.ds.PGPoolingDataSource;
import org.springframework.cloud.servicebroker.model.BrokerApiVersion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

import static io.pivotal.ecosystem.servicebroker.PostgresClient.*;

@Configuration
@ComponentScan(basePackages = {"io.pivotal.ecosystem.servicebroker", "io.pivotal.cf.servicebroker", "io.pivotal.ecosystem.sqlserver"})
public class PostgresConfig {

    @Bean
    public DataSource datasource(Environment env) {
        PGPoolingDataSource source = new PGPoolingDataSource();
        source.setServerName(env.getProperty(POSTGRES_HOST_KEY));
        source.setDatabaseName(env.getProperty(POSTGRES_DB));
        source.setUser(env.getProperty(POSTGRES_USER));
        source.setPassword(env.getProperty(POSTGRES_PASSWORD));
        return source;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource datasource) {
        return new JdbcTemplate(datasource);
    }

    @Bean
    public String dbUrl(Environment env) {
        return POSTGRES_URI_SCHEME + "://" + env.getProperty(POSTGRES_HOST_KEY) + ":" + Integer.parseInt(env.getProperty(POSTGRES_PORT_KEY));
    }

    @Bean
    public BrokerApiVersion brokerApiVersion() {
        return new BrokerApiVersion();
    }
}