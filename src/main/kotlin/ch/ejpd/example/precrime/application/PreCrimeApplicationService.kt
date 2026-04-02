package ch.ejpd.example.precrime.application

import ch.ejpd.example.precrime.domain.enforcement.LawEnforcementRepository
import ch.ejpd.example.precrime.domain.enforcement.PreArrestExecutedEvent
import ch.ejpd.example.precrime.domain.precog.CrimeForeseen
import ch.ejpd.example.precrime.domain.precog.PrecogDivisionRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@org.jmolecules.ddd.annotation.Service
@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
class PreCrimeApplicationService(
    private val precogRepository: PrecogDivisionRepository,
    private val enforcementRepository: LawEnforcementRepository
) {
    val logger = LoggerFactory.getLogger(javaClass)

    @Transactional(readOnly = true)
    fun getPreventedCrimesCount(): Int {
        return precogRepository.findSingleton().totalCrimesPrevented
    }

    fun triggerVision(perpetrator: String, crimeType: String) {
        val division = precogRepository.findSingleton()
        division.foreseeCrime(perpetrator, crimeType)
        precogRepository.save(division)
        logger.info("🔮 [PrecogDivision] Foresee: $perpetrator will commit $crimeType! Aggregate published event.")
    }

    fun onCrimeForeseen(event: CrimeForeseen) {
        logger.info("🚓 [LawEnforcement] Received vision: ${event.perpetrator} planning ${event.crimeType}. Deploying jetpacks!")
        val unit = enforcementRepository.findSingleton()
        unit.executePreArrest(event.visionId, event.perpetrator)
        enforcementRepository.save(unit)
    }

    fun onPreArrestExecuted(event: PreArrestExecutedEvent) {
        logger.info("✅ [PrecogDivision] Received pre-arrest confirmation for ${event.perpetrator}. Updating stats.")
        val division = precogRepository.findSingleton()
        division.recordPrevention()
        precogRepository.save(division)
        logger.info("📊 Stats: Total crimes 'prevented' via Minority Report logic: ${division.totalCrimesPrevented}")
    }
}
