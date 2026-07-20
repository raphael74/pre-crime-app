package org.example.precrime.application

import org.example.precrime.domain.DomainEventPublisher
import org.example.precrime.domain.perpetrator.Perpetrator
import org.example.precrime.domain.perpetrator.PerpetratorId
import org.example.precrime.domain.perpetrator.PerpetratorRepository
import org.example.precrime.domain.preapology.PreApology
import org.example.precrime.domain.preapology.PreApologyDomainService
import org.example.precrime.domain.preapology.PreApologyRepository
import org.example.precrime.domain.prearrest.PreArrest
import org.example.precrime.domain.prearrest.PreArrestExecutedEvent
import org.example.precrime.domain.prearrest.PreArrestId
import org.example.precrime.domain.prearrest.PreArrestRepository
import org.example.precrime.domain.statistic.StatisticRepository
import org.example.precrime.domain.vision.*
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
    private val perpetratorRepository: PerpetratorRepository,
    private val preApologyDomainService: PreApologyDomainService,
    private val publisher: DomainEventPublisher
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional(readOnly = true)
    fun getPreventedCrimesCount(): Int {
        return statisticRepository.findSingleton().totalCrimesPrevented
    }

    @Transactional(readOnly = true)
    fun getAllApologies(): List<PreApologyWithPerpetrator> {
        logger.info("Getting all Apologies")
        val apologies = preApologyRepository.findAll()
        return apologies.map { apology ->
            PreApologyWithPerpetrator(apology, perpetratorRepository.findById(apology.perpetratorId)!!)
        }
    }

    @Transactional(readOnly = true)
    fun getAllPendingPreArrests(): List<PreArrestWithPerpetrator> {
        logger.info("Getting all pending PreArrests")
        val arrests = preArrestRepository.findAllPending()
        return arrests.map { arrest ->
            PreArrestWithPerpetrator(arrest, perpetratorRepository.findById(arrest.perpetratorId)!!)
        }
    }

    @Transactional(readOnly = true)
    fun getAllExecutedPreArrests(): List<PreArrestWithPerpetrator> {
        logger.info("Getting executed PreArrests")
        val arrests = preArrestRepository.findAllArrested()
        return arrests.map { arrest ->
            PreArrestWithPerpetrator(arrest, perpetratorRepository.findById(arrest.perpetratorId)!!)
        }
    }

    fun triggerVision(cmd: CreateVisionCommand): VisionId {
        logger.info("Triggering vision. ${cmd.perpetratorFirstName} ${cmd.perpetratorFirstName} will commit ${cmd.crimeType.value}")
        var perpetrator = perpetratorRepository.findByFirstAndLastName(
            cmd.perpetratorFirstName,
            cmd.perpetratorLastName
        )
        if (perpetrator == null) {
            perpetrator = Perpetrator(
                firstName = cmd.perpetratorFirstName,
                lastName = cmd.perpetratorLastName
            )
            perpetratorRepository.create(perpetrator)
        }
        val vision = VisionFactory.createVision(perpetrator.id, cmd.crimeType)
        vision.injectPublisher(publisher)
        visionRepository.create(vision)
        vision.foreseeCrime()
        return vision.id
    }

    fun executePreArrest(preArrestId: PreArrestId) {
        logger.info("Executing PreArrest")
        val preArrest = preArrestRepository.findById(preArrestId)
            ?: throw IllegalStateException("PreArrest $preArrestId not found")
        preArrest.injectPublisher(publisher)
        preArrest.executePreArrest()
        preArrestRepository.update(preArrest)
    }

    fun cancelPreArrest(preArrestId: PreArrestId) {
        logger.info("Cancelling PreArrest")
        val preArrest = preArrestRepository.findById(preArrestId)
            ?: throw IllegalStateException("PreArrest $preArrestId not found")
        preArrest.injectPublisher(publisher)
        preArrest.cancelPreArrest()
        preArrestRepository.update(preArrest)
    }

    @DomainEventHandler
    fun onCrimeForeseen(event: CrimeForeseenEvent) {
        val preArrest = PreArrest(visionId = event.visionId, perpetratorId = event.perpetratorId)
        preArrestRepository.create(preArrest)
    }

    @DomainEventHandler
    fun onPreArrestExecuted(event: PreArrestExecutedEvent) {
        updateStatistics()
        createPreApology(event.perpetratorId, event.preArrestId, event.visionId)
    }

    private fun updateStatistics() {
        val statistic = statisticRepository.findSingleton()
        statistic.recordPrevention()
        statisticRepository.update(statistic)
        logger.info("Stats: Total crimes 'prevented' via Minority Report logic: ${statistic.totalCrimesPrevented}")

    }

    private fun createPreApology(perpetratorId: PerpetratorId, preArrestId: PreArrestId, visionId: VisionId) {
        // Generate and save pre-emptive apology & compensation statement
        val vision = visionRepository.findById(visionId)
            ?: throw IllegalStateException("Vision $visionId not found")

        val preApology = preApologyDomainService.generatePreApology(preArrestId, perpetratorId, vision.crimeType)
        preApology.injectPublisher(publisher)
        preApology.issue()
        preApologyRepository.create(preApology)

        logger.info("Issued pre-emptive apology to ${preApology.perpetratorId}. Net payout: ${preApology.compensation.netPayout}")
    }
}

data class PreArrestWithPerpetrator(val preArrest: PreArrest, val perpetrator: Perpetrator)
data class PreApologyWithPerpetrator(val apology: PreApology, val perpetrator: Perpetrator)

data class CreateVisionCommand(
    val perpetratorFirstName: String,
    val perpetratorLastName: String,
    val crimeType: CrimeType
)