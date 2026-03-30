package com.web.hotdeal.commons.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("핫딜 모음 API")
                        .version("v1")
                        .description("핫딜 수집/조회 백엔드 API")
                        .contact(new Contact().name("hotdeal-moa backend")));
    }
}
