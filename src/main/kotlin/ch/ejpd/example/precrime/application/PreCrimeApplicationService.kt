package ch.ejpd.example.precrime.application

import ch.ejpd.example.precrime.domain.enforcement.LawEnforcementRepository
import ch.ejpd.example.precrime.domain.enforcement.PreArrestExecutedEvent
import ch.ejpd.example.precrime.domain.precog.CrimeForeseenEvent
import ch.ejpd.example.precrime.domain.precog.CrimeType
import ch.ejpd.example.precrime.domain.precog.Perpetrator
import ch.ejpd.example.precrime.domain.precog.PrecogDivisionRepository
import org.jmolecules.event.annotation.DomainEventHandler
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@org.jmolecules.ddd.annotation.Service
@Service
@Transactional
class PreCrimeApplicationService(
    private val precogRepository: PrecogDivisionRepository,
    private val enforcementRepository: LawEnforcementRepository
) {
    val logger = LoggerFactory.getLogger(javaClass)

    @Transactional(readOnly = true)
    fun getPreventedCrimesCount(): Int {
        return precogRepository.findSingleton().totalCrimesPrevented
    }

    fun triggerVision(perpetratorName: String, crimeTypeName: String) {
        val division = precogRepository.findSingleton()
        division.foreseeCrime(Perpetrator(perpetratorName), CrimeType(crimeTypeName))
        precogRepository.save(division)
        logger.info("🔮 [PrecogDivision] Foresee: $perpetratorName will commit $crimeTypeName! Aggregate published event.")
    }

    @DomainEventHandler
    fun onCrimeForeseen(event: CrimeForeseenEvent) {
        logger.info("🚓 [LawEnforcement] Received vision: ${event.perpetrator.name} planning ${event.crimeType.value}. Deploying jetpacks!")
        val unit = enforcementRepository.findSingleton()
        unit.executePreArrest(event.visionId, event.perpetrator)
        enforcementRepository.save(unit)
    }

    @DomainEventHandler
    fun onPreArrestExecuted(event: PreArrestExecutedEvent) {
        logger.info("✅ [PrecogDivision] Received pre-arrest confirmation for ${event.perpetrator.name}. Updating stats.")
        val division = precogRepository.findSingleton()
        division.recordPrevention()
        precogRepository.save(division)
        logger.info("📊 Stats: Total crimes 'prevented' via Minority Report logic: ${division.totalCrimesPrevented}")
    }
}
