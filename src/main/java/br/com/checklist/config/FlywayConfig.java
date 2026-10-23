package br.com.checklist.config;

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfig {

    /**
     * Executa repair() antes de migrate() para limpar eventuais migrações interrompidas
     * (success = false) e realinhar checksums automaticamente em ambientes de nuvem (Render/Neon).
     */
    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            flyway.repair();
            flyway.migrate();
        };
    }
}
