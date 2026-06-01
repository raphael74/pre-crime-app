package ch.ejpd.example.precrime.application

import ch.ejpd.example.precrime.domain.DomainEventPublisher
import ch.ejpd.example.precrime.domain.apology.PreApology
import ch.ejpd.example.precrime.domain.apology.PreApologyRepository
import ch.ejpd.example.precrime.domain.apology.PreEmptiveApologyDomainService
import ch.ejpd.example.precrime.domain.prearrest.PreArrest
import ch.ejpd.example.precrime.domain.prearrest.PreArrestExecutedEvent
import ch.ejpd.example.precrime.domain.prearrest.PreArrestRepository
import ch.ejpd.example.precrime.domain.statistic.StatisticRepository
import ch.ejpd.example.precrime.domain.vision.*
import org.jmolecules.event.annotation.DomainEventHandler
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@org.jmolecules.ddd.annotation.Service
@Service
@Transactional
class PreCrimeApplicationService(
    private val visionRepository: VisionRepository,
    private val statisticRepository: StatisticRepository,
    private val preArrestRepository: PreArrestRepository,
    private val preApologyRepository: PreApologyRepository,
    private val preEmptiveApologyDomainService: PreEmptiveApologyDomainService,
    private val publisher: DomainEventPublisher
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional(readOnly = true)
    fun getPreventedCrimesCount(): Int {
        return statisticRepository.findSingleton().totalCrimesPrevented
    }

    @Transactional(readOnly = true)
    fun getAllApologies(): List<PreApology> {
        return preApologyRepository.findAll()
    }

    @Transactional(readOnly = true)
    fun getAllPreArrests(): List<PreArrest> {
        return preArrestRepository.findAll()
    }

    fun triggerVision(perpetratorFirstName: String, perpetratorLastName: String, crimeType: CrimeType): VisionId {
        val vision = VisionFactory.createVision(Perpetrator(perpetratorFirstName, perpetratorLastName), crimeType)
        visionRepository.create(vision)
        vision.foreseeCrime()
        publisher.publish(vision.domainEvents)
        vision.clearDomainEvents()
        logger.info("[Vision] Foresee: $perpetratorFirstName $perpetratorLastName will commit ${crimeType.value}! Aggregate published event.")
        return vision.id
    }

    @DomainEventHandler
    fun onCrimeForeseen(event: CrimeForeseenEvent) {
        logger.info("[LawEnforcement] Received vision: ${event.perpetrator.fullName} planning ${event.crimeType.value}. Deploying jetpacks!")
        val preArrest = PreArrest(visionId = event.visionId, perpetrator = event.perpetrator)
        preArrestRepository.save(preArrest)
        publisher.publish(preArrest.domainEvents)
        preArrest.clearDomainEvents()
    }

    @DomainEventHandler
    fun onPreArrestExecuted(event: PreArrestExecutedEvent) {
        logger.info("[LawEnforcement] Received pre-arrest confirmation for ${event.perpetrator.fullName}. Updating stats.")
        val statistic = statisticRepository.findSingleton()
        statistic.recordPrevention()
        statisticRepository.update(statistic)
        logger.info("Stats: Total crimes 'prevented' via Minority Report logic: ${statistic.totalCrimesPrevented}")

        // Generate and save pre-emptive apology & compensation statement
        val vision = visionRepository.findById(event.visionId)
            ?: throw IllegalStateException("Vision ${event.visionId} not found")

        val apology = preEmptiveApologyDomainService.generateApology(vision)
        preApologyRepository.save(apology)

        publisher.publish(apology.domainEvents)
        apology.clearDomainEvents()
        logger.info("[PreApologyService] Issued pre-emptive apology to ${apology.perpetrator.fullName}. Net payout: ${apology.compensation.netPayout}")
    }
}
