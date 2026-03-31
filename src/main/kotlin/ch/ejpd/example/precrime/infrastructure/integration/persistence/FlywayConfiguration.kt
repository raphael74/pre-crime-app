package ch.ejpd.example.precrime.infrastructure.integration.persistence

import org.flywaydb.core.api.FlywayException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FlywayConfiguration {
    val logger = LoggerFactory.getLogger(javaClass)

    @Value("\${precrime.flyway.clean-on-validation-error:false}")
    private var cleanOnValidationError: Boolean = false

    @Bean
    fun flywayMigrationStrategy(): FlywayMigrationStrategy {
        return FlywayMigrationStrategy { flyway ->
            try {
                flyway.migrate()
            } catch (e: FlywayException) {
                if (cleanOnValidationError) {
                    logger.info("Flyway migration failed! Cleaning the database before retrying", e)
                    flyway.clean()
                    flyway.migrate() // 2nd try after clean
                }
            }
        }
    }
}