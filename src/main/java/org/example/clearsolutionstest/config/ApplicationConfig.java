package org.example.clearsolutionstest.config;

import lombok.Generated;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@Generated
public class ApplicationConfig {

    @Value("${application.properties.minimal-user-age}")
    private Integer minimalUserAge;

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    @Qualifier("minimalUserAge")
    public Integer getMinimalUserAge() {
        return minimalUserAge;
    }

}
