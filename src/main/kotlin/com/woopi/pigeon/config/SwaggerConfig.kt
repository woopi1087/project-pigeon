package com.woopi.pigeon.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun openAPI(): OpenAPI = OpenAPI()
        .info(
            Info()
                .title("Pigeon API")
                .description("메시지 발송 서비스 — FCM / SMS / Email / Kakao")
                .version("v1")
        )
}
