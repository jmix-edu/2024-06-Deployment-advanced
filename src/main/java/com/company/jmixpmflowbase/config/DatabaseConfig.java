package com.company.jmixpmflowbase.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.vault.core.VaultTemplate;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    private final VaultTemplate vaultTemplate;

    @Primary
    @Profile("default")
    @Bean("dataSourceProperties")
    @ConfigurationProperties("main.datasource")
    DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConfigurationProperties("main.datasource.hikari")
    DataSource dataSource(final DataSourceProperties dataSourceProperties) {
        return dataSourceProperties.initializeDataSourceBuilder().build();
    }

    public DatabaseConfig(@Lazy VaultTemplate vaultTemplate) {
        this.vaultTemplate = vaultTemplate;
    }

    @Profile("prod")
    @Bean("dataSourceProperties")
    DataSourceProperties dataSourcePropertiesVault() {
        DataSourceProperties properties = new DataSourceProperties();
        DbConfig dbConfig = new ObjectMapper().convertValue(
                vaultTemplate.read("secret/data/application/database/credentials").getData().get("data"),
                DbConfig.class);

        properties.setUrl(dbConfig.getUrl());
        properties.setPassword(dbConfig.getPassword());
        properties.setUsername(dbConfig.getUsername());

        return properties;
    }
}
