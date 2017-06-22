package io.pivotal.ecosystem.servicebroker;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PostgresBrokerTest {

    public static final String HOST = "jdbc:postgresql://104.154.149.54:5432/template1";

    @Test
    public void testPostgresConnection() throws Exception {
        Class.forName("org.postgresql.Driver");
        Connection conn = DriverManager.getConnection(HOST,"vcap", "pivotal123");
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("CREATE DATABASE TestDB");
        stmt.executeUpdate("CREATE USER test_user WITH PASSWORD 'pivotal'");
        stmt.executeUpdate("GRANT ALL PRIVILEGES ON DATABASE TestDB to test_user");
        stmt.executeUpdate("DROP DATABASE IF EXISTS TestDB");
        stmt.executeUpdate("DROP USER IF EXISTS test_user");
    }



}
