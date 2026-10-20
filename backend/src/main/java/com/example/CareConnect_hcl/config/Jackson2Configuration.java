package com.example.CareConnect_hcl.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Jackson2Configuration {
    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper careConnectObjectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }
}
