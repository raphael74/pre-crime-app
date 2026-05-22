package ch.ejpd.example.precrime.infrastructure

import ch.ejpd.example.precrime.domain.apology.PreEmptiveApologyDomainService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DomainServicesConfiguration {

    @Bean
    fun preEmptiveApologyDomainService(): PreEmptiveApologyDomainService {
        return PreEmptiveApologyDomainService()
    }
}