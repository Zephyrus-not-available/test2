package com.KTU.KTUVotingapp.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

    @Value("${DATABASE_PASSWORD:#{null}}")
    private String databasePassword;

    @Bean
    @ConditionalOnProperty(name = "DATABASE_URL")
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

            // Build JDBC URL
            String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", host, port, database);

            dataSource.setJdbcUrl(jdbcUrl);
            dataSource.setUsername(username);
            dataSource.setPassword(password);
        } else {
            // Fallback: use individual properties
            dataSource.setJdbcUrl(System.getenv("SPRING_DATASOURCE_URL"));
            dataSource.setUsername(System.getenv("SPRING_DATASOURCE_USERNAME"));
            dataSource.setPassword(databasePassword != null ? databasePassword : System.getenv("SPRING_DATASOURCE_PASSWORD"));
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

