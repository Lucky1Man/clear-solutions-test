package org.example.clearsolutionstest.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = "org.example.clearsolutionstest.entity")
@EnableJpaRepositories(basePackages = "org.example.clearsolutionstest.repository")
public class TestRepositoryConfig {
}
