// src/main/java/com/Globoo/common/config/SwaggerConfig.java
package com.Globoo.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        String schemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("Globoo API")
                        .description("Globoo Backend API 문서")
                        .version("v1"))
                .components(new Components()
                        .addSecuritySchemes(schemeName,
                                new SecurityScheme()
                                        .name(schemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(schemeName));
    }

    // /api/** 전부 노출 (auth/keywords/languages/countries 포함)
    @Bean
    public GroupedOpenApi apiAll() {
        return GroupedOpenApi.builder()
                .group("api")
                .pathsToMatch("/api/**")
                .build();
    }

    // (선택) 문제가 계속되면 패키지 스캔을 명시적으로 추가
    @Bean
    public GroupedOpenApi userApis() {
        return GroupedOpenApi.builder()
                .group("user-modules")
                .packagesToScan("com.Globoo.user.web") // CountryController 등
                .pathsToMatch("/api/**")
                .build();
    }
}
