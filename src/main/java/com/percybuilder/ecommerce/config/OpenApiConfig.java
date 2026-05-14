package com.percybuilder.ecommerce.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Spring Boot Ecommerce API",
                version = "v1",
                description = """
                        REST API for an ecommerce backend built with Spring Boot, PostgreSQL, JPA, JWT authentication,
                        role-based admin authorization, cart management, address management, order creation,
                        and payment status handling.
                        """,
                contact = @Contact(
                        name = "Percy Osunde",
                        email = "percyosag@gmail.com"
                )
        ),
        servers = {
                @Server(
                        url = "http://localhost:8080",
                        description = "Local development server"
                )
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
}