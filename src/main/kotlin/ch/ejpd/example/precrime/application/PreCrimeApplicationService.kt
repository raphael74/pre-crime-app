package ch.ejpd.example.precrime.application

import ch.ejpd.example.precrime.application.event.DomainEventPublisher
import ch.ejpd.example.precrime.domain.enforcement.LawEnforcementRepository
import ch.ejpd.example.precrime.domain.enforcement.PreArrestExecuted
import ch.ejpd.example.precrime.domain.precog.CrimeForeseen
import ch.ejpd.example.precrime.domain.precog.PrecogDivisionRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@org.jmolecules.ddd.annotation.Service
@Service
class PreCrimeApplicationService(
    private val precogRepository: PrecogDivisionRepository,
    private val enforcementRepository: LawEnforcementRepository,
    private val domainEventPublisher: DomainEventPublisher
) {
    val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun triggerVision(perpetrator: String, crimeType: String) {
        val division = precogRepository.findSingleton()
        val event = division.foreseeCrime(perpetrator, crimeType)
        precogRepository.save(division)
        logger.info("🔮 [PrecogDivision] Foresee: $perpetrator will commit $crimeType! Publishing event...")
        domainEventPublisher.publish(event)
    }

    @Transactional
    fun onCrimeForeseen(event: CrimeForeseen) {
        logger.info("🚓 [LawEnforcement] Received vision: ${event.perpetrator} planning ${event.crimeType}. Deploying jetpacks!")
        val unit = enforcementRepository.findSingleton()
        val arrestEvent = unit.executePreArrest(event.visionId, event.perpetrator)
        enforcementRepository.save(unit)
        domainEventPublisher.publish(arrestEvent)
    }

    @Transactional
    fun onPreArrestExecuted(event: PreArrestExecuted) {
        logger.info("✅ [PrecogDivision] Received pre-arrest confirmation for ${event.perpetrator}. Updating stats.")
        val division = precogRepository.findSingleton()
        division.recordPrevention()
        precogRepository.save(division)
        logger.info("📊 Stats: Total crimes 'prevented' via Minority Report logic: ${division.totalCrimesPrevented}")
    }
}
