package com.staffbase.employee_record_system.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("StaffBase Employee Record System API")
                        .version("1.0")
                        .description(
                                "Comprehensive HR management system API for employee tracking, payroll, and performance.")
                        .termsOfService("http://staffbase.com/terms")
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")));
    }
}



