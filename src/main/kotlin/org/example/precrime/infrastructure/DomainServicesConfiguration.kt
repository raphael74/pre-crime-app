package org.example.precrime.infrastructure

import org.example.precrime.domain.preapology.PreApologyDomainService
import org.example.precrime.domain.preapology.PreApologyLetterService
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