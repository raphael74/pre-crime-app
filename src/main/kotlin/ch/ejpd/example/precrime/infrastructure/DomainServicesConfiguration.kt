package ch.ejpd.example.precrime.infrastructure

import ch.ejpd.example.precrime.domain.DomainEventPublisher
import ch.ejpd.example.precrime.domain.apology.PreApologyLetterService
import ch.ejpd.example.precrime.domain.apology.PreEmptiveApologyDomainService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DomainServicesConfiguration {

    @Bean
    fun preEmptiveApologyDomainService(
        preApologyLetterService: PreApologyLetterService,
        publisher: DomainEventPublisher
    ): PreEmptiveApologyDomainService {
        return PreEmptiveApologyDomainService(preApologyLetterService, publisher)
    }
}