package com.odontosystem.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de la documentación interactiva de la API (Swagger UI).
 * Disponible en /swagger-ui.html una vez que el backend está corriendo,
 * y el JSON crudo de OpenAPI en /v3/api-docs.
 *
 * Refuerza el criterio de "documentación técnica" del proyecto: permite
 * explorar y probar los 5 endpoints (login, dentists, appointments,
 * chatbot) directamente desde el navegador, sin necesitar Postman.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI odontoSystemOpenApi() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("OdontoSystem API")
                        .description("Backend REST del Taller ABP — Diseño de Aplicaciones Móviles (FUCN). "
                                + "Gestión de citas odontológicas con asistente conversacional.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Nicole De La Cruz Chávez y Luana Barrientos Espinoza")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Pega aquí el token obtenido en POST /api/v1/auth/login "
                                        + "(sin la palabra 'Bearer', Swagger la agrega automáticamente).")));
    }
}
