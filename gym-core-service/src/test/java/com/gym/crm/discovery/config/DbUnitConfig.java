package com.gym.crm.discovery.config;

import com.github.springtestdbunit.bean.DatabaseConfigBean;
import com.github.springtestdbunit.bean.DatabaseDataSourceConnectionFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DbUnitConfig {
    @Bean
    public DatabaseConfigBean dbUnitDatabaseConfig() {
        DatabaseConfigBean config = new DatabaseConfigBean();
        config.setQualifiedTableNames(false);
        config.setDatatypeFactory(new org.dbunit.ext.h2.H2DataTypeFactory());

        return config;
    }

    @Bean
    public DatabaseDataSourceConnectionFactoryBean dbUnitDatabaseConnection(DataSource dataSource, DatabaseConfigBean dbUnitDatabaseConfig) {
        DatabaseDataSourceConnectionFactoryBean factory = new DatabaseDataSourceConnectionFactoryBean(dataSource);
        factory.setDatabaseConfig(dbUnitDatabaseConfig);
        factory.setSchema("PUBLIC");

        return factory;
    }
}
