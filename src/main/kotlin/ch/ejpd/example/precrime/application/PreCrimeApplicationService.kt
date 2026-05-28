package ch.ejpd.example.precrime.application

import ch.ejpd.example.precrime.domain.DomainEventPublisher
import ch.ejpd.example.precrime.domain.apology.PreApology
import ch.ejpd.example.precrime.domain.apology.PreApologyRepository
import ch.ejpd.example.precrime.domain.apology.PreEmptiveApologyDomainService
import ch.ejpd.example.precrime.domain.enforcement.LawEnforcementRepository
import ch.ejpd.example.precrime.domain.enforcement.PreArrestExecutedEvent
import ch.ejpd.example.precrime.domain.precog.*
import org.jmolecules.event.annotation.DomainEventHandler
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@org.jmolecules.ddd.annotation.Service
@Service
@Transactional
class PreCrimeApplicationService(
    private val precogRepository: PrecogDivisionRepository,
    private val enforcementRepository: LawEnforcementRepository,
    private val preApologyRepository: PreApologyRepository,
    private val preEmptiveApologyDomainService: PreEmptiveApologyDomainService,
    private val publisher: DomainEventPublisher
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional(readOnly = true)
    fun getPreventedCrimesCount(): Int {
        return precogRepository.findSingleton().totalCrimesPrevented
    }

    @Transactional(readOnly = true)
    fun getAllApologies(): List<PreApology> {
        return preApologyRepository.findAll()
    }

    fun triggerVision(perpetratorFirstName: String, perpetratorLastName: String, crimeType: CrimeType): VisionId {
        val division = precogRepository.findSingleton()
        val visionId = division.foreseeCrime(Perpetrator(perpetratorFirstName, perpetratorLastName), crimeType)
        precogRepository.update(division)
        publisher.publish(division.domainEvents)
        division.clearDomainEvents()
        logger.info("[PrecogDivision] Foresee: $perpetratorFirstName $perpetratorLastName will commit ${crimeType.value}! Aggregate published event.")
        return visionId
    }

    @DomainEventHandler
    fun onCrimeForeseen(event: CrimeForeseenEvent) {
        logger.info("[LawEnforcement] Received vision: ${event.perpetrator.fullName} planning ${event.crimeType.value}. Deploying jetpacks!")
        val unit = enforcementRepository.findSingleton()
        unit.executePreArrest(event.visionId, event.perpetrator)
        enforcementRepository.update(unit)
        publisher.publish(unit.domainEvents)
        unit.clearDomainEvents()
    }

    @DomainEventHandler
    fun onPreArrestExecuted(event: PreArrestExecutedEvent) {
        logger.info("[PrecogDivision] Received pre-arrest confirmation for ${event.perpetrator.fullName}. Updating stats.")
        val division = precogRepository.findSingleton()
        division.recordPrevention()
        precogRepository.update(division)
        publisher.publish(division.domainEvents)
        division.clearDomainEvents()
        logger.info("Stats: Total crimes 'prevented' via Minority Report logic: ${division.totalCrimesPrevented}")

        // Generate and save pre-emptive apology & compensation statement
        val vision = division.visions.find { it.id == event.visionId }
            ?: throw IllegalStateException("Vision ${event.visionId} not found in division")

        val apology = preEmptiveApologyDomainService.generateApology(vision)
        preApologyRepository.save(apology)

        publisher.publish(apology.domainEvents)
        apology.clearDomainEvents()
        logger.info("[PreApologyService] Issued pre-emptive apology to ${apology.perpetrator.fullName}. Net payout: ${apology.compensation.netPayout}")
    }
}
