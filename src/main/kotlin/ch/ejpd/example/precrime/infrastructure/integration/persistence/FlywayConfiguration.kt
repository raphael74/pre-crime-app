package ch.ejpd.example.precrime.infrastructure.integration.persistence

import org.flywaydb.core.api.FlywayException
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FlywayConfiguration {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Bean
    @ConditionalOnProperty(prefix = "precrime.flyway", name = ["clean-on-validation-error"])
    fun flywayMigrationStrategy(): FlywayMigrationStrategy {
        return FlywayMigrationStrategy { flyway ->
            try {
                flyway.migrate()
            } catch (e: FlywayException) {
                logger.info("Flyway migration failed! Cleaning the database before retrying", e)
                flyway.clean()
                flyway.migrate() // 2nd try after clean
            }
        }
    }
}