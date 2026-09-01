package com.odontosystem.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Prueba de humo: verifica que el contexto de Spring Boot cargue
 * correctamente todas las configuraciones (seguridad, JPA, etc.).
 * Requiere una base de datos accesible (usa el perfil "test", ver
 * application-test.properties) para que Flyway pueda ejecutar las
 * migraciones durante la carga del contexto.
 */
@SpringBootTest
@ActiveProfiles("test")
class OdontoSystemApiApplicationTests {

    @Test
    void contextLoads() {
        // Si el contexto de Spring carga sin lanzar excepciones, la prueba pasa.
    }
}
