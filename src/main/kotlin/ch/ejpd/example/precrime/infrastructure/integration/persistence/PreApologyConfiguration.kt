package ch.ejpd.example.precrime.infrastructure.integration.persistence

import ch.ejpd.example.precrime.domain.apology.PreEmptiveApologyDomainService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PreApologyConfiguration {

    @Bean
    fun preEmptiveApologyService(): PreEmptiveApologyDomainService {
        return PreEmptiveApologyDomainService()
    }
}
