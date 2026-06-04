package ch.ejpd.example.precrime.infrastructure

import ch.ejpd.example.precrime.domain.preapology.PreApologyDomainService
import ch.ejpd.example.precrime.domain.preapology.PreApologyLetterService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DomainServicesConfiguration {

    @Bean
    fun preEmptiveApologyDomainService(
        preApologyLetterService: PreApologyLetterService
    ): PreApologyDomainService {
        return PreApologyDomainService(preApologyLetterService)
    }
}