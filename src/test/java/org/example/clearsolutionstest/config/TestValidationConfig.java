package org.example.clearsolutionstest.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EntityScan(basePackages = "org.example.clearsolutionstest.entity")
@Import(ValidationAutoConfiguration.class)
public class TestValidationConfig {
}
