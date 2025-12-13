package com.KTU.KTUVotingapp.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Database configuration for production environment.
 * Handles Koyeb's postgres:// URL format and converts it to jdbc:postgresql:// format.
 */
@Configuration
@Profile("prod")
public class DatabaseConfig {

    @Value("${DATABASE_URL:#{null}}")
    private String databaseUrl;

    @Bean
    @Primary
    public DataSource dataSource() throws URISyntaxException {
        HikariDataSource dataSource = new HikariDataSource();

        if (databaseUrl != null && !databaseUrl.isEmpty()) {
            // Parse the postgres:// URL format from Koyeb
            URI dbUri = new URI(databaseUrl);

            String username = dbUri.getUserInfo().split(":")[0];
            String password = dbUri.getUserInfo().split(":")[1];
            String host = dbUri.getHost();
            int port = dbUri.getPort() == -1 ? 5432 : dbUri.getPort();
            String database = dbUri.getPath().substring(1); // Remove leading '/'

            // Build JDBC URL with SSL for Koyeb
            String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s?sslmode=require", host, port, database);

            System.out.println("Connecting to database: " + host + ":" + port + "/" + database);

            dataSource.setJdbcUrl(jdbcUrl);
            dataSource.setUsername(username);
            dataSource.setPassword(password);
        } else {
            // Fallback: use individual properties from environment
            String url = System.getenv("SPRING_DATASOURCE_URL");
            String username = System.getenv("SPRING_DATASOURCE_USERNAME");
            String password = System.getenv("SPRING_DATASOURCE_PASSWORD");

            if (url == null || url.isEmpty()) {
                throw new RuntimeException("DATABASE_URL or SPRING_DATASOURCE_URL environment variable must be set");
            }

            dataSource.setJdbcUrl(url);
            dataSource.setUsername(username);
            dataSource.setPassword(password);
        }

        dataSource.setDriverClassName("org.postgresql.Driver");

        // Connection pool settings
        dataSource.setMaximumPoolSize(12);
        dataSource.setMinimumIdle(2);
        dataSource.setConnectionTimeout(30000);
        dataSource.setIdleTimeout(600000);
        dataSource.setMaxLifetime(1800000);

        return dataSource;
    }
}

