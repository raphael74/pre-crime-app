package ch.ejpd.example.precrime.application

import ch.ejpd.example.precrime.domain.DomainEventPublisher
import ch.ejpd.example.precrime.domain.apology.PreApology
import ch.ejpd.example.precrime.domain.apology.PreApologyRepository
import ch.ejpd.example.precrime.domain.apology.PreEmptiveApologyDomainService
import ch.ejpd.example.precrime.domain.perpetrator.Perpetrator
import ch.ejpd.example.precrime.domain.perpetrator.PerpetratorRepository
import ch.ejpd.example.precrime.domain.prearrest.PreArrest
import ch.ejpd.example.precrime.domain.prearrest.PreArrestExecutedEvent
import ch.ejpd.example.precrime.domain.prearrest.PreArrestId
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
    private val perpetratorRepository: PerpetratorRepository,
    private val preEmptiveApologyDomainService: PreEmptiveApologyDomainService,
    private val publisher: DomainEventPublisher
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional(readOnly = true)
    fun getPreventedCrimesCount(): Int {
        return statisticRepository.findSingleton().totalCrimesPrevented
    }

    @Transactional(readOnly = true)
    fun getAllApologies(): List<PreApologyWithPerpetrator> {
        val apologies = preApologyRepository.findAll()
        val perpetratorIds = apologies.map { it.perpetratorId }.toSet()
        val perpetrators = perpetratorRepository.findByIds(perpetratorIds).associateBy { it.id }
        return apologies.map { apology ->
            PreApologyWithPerpetrator(apology, perpetrators[apology.perpetratorId]!!)
        }
    }

    @Transactional(readOnly = true)
    fun getAllPendingPreArrests(): List<PreArrestWithPerpetrator> {
        val arrests = preArrestRepository.findAllPending()
        val perpetratorIds = arrests.map { it.perpetratorId }.toSet()
        val perpetrators = perpetratorRepository.findByIds(perpetratorIds).associateBy { it.id }
        return arrests.map { arrest ->
            PreArrestWithPerpetrator(arrest, perpetrators[arrest.perpetratorId]!!)
        }
    }

    @Transactional(readOnly = true)
    fun getAllExecutedPreArrests(): List<PreArrestWithPerpetrator> {
        val arrests = preArrestRepository.findAllArrested()
        val perpetratorIds = arrests.map { it.perpetratorId }.toSet()
        val perpetrators = perpetratorRepository.findByIds(perpetratorIds).associateBy { it.id }
        return arrests.map { arrest ->
            PreArrestWithPerpetrator(arrest, perpetrators[arrest.perpetratorId]!!)
        }
    }

    fun triggerVision(cmd: CreateVisionCommand): VisionId {
        var perpetrator = perpetratorRepository.findByFirstAndLastName(
            cmd.perpetratorFirstName,
            cmd.perpetratorLastName
        )
        if (perpetrator == null) {
            perpetrator = Perpetrator(
                firstName = cmd.perpetratorFirstName,
                lastName = cmd.perpetratorLastName
            )
            perpetratorRepository.save(perpetrator)
        }
        val vision = VisionFactory.createVision(perpetrator.id, cmd.crimeType)
        vision.injectPublisher(publisher)
        visionRepository.create(vision)
        vision.foreseeCrime()
        logger.info("Foresee: $cmd.perpetratorFirstName $cmd.perpetratorLastName will commit ${cmd.crimeType.value}!")
        return vision.id
    }

    fun executePreArrest(preArrestId: PreArrestId) {
        val preArrest = preArrestRepository.findById(preArrestId)
            ?: throw IllegalStateException("PreArrest $preArrestId not found")
        preArrest.injectPublisher(publisher)
        preArrest.executePreArrest()
        preArrestRepository.save(preArrest)
    }

    fun cancelPreArrest(preArrestId: PreArrestId) {
        val preArrest = preArrestRepository.findById(preArrestId)
            ?: throw IllegalStateException("PreArrest $preArrestId not found")
        preArrest.injectPublisher(publisher)
        preArrest.cancelPreArrest()
        preArrestRepository.save(preArrest)
    }

    @DomainEventHandler
    fun onCrimeForeseen(event: CrimeForeseenEvent) {
        val preArrest = PreArrest(visionId = event.visionId, perpetratorId = event.perpetratorId)
        preArrestRepository.save(preArrest)
    }

    @DomainEventHandler
    fun onPreArrestExecuted(event: PreArrestExecutedEvent) {
        updateStatistics()
        createPreApology(event.visionId)

    }

    private fun updateStatistics() {
        val statistic = statisticRepository.findSingleton()
        statistic.recordPrevention()
        statisticRepository.update(statistic)
        logger.info("Stats: Total crimes 'prevented' via Minority Report logic: ${statistic.totalCrimesPrevented}")

    }

    private fun createPreApology(visionId: VisionId) {
        // Generate and save pre-emptive apology & compensation statement
        val vision = visionRepository.findById(visionId)
            ?: throw IllegalStateException("Vision $visionId not found")

        val apology = preEmptiveApologyDomainService.generateApology(vision)
        preApologyRepository.save(apology)

        logger.info("Issued pre-emptive apology to ${apology.perpetratorId}. Net payout: ${apology.compensation.netPayout}")
    }
}

data class PreArrestWithPerpetrator(val preArrest: PreArrest, val perpetrator: Perpetrator)
data class PreApologyWithPerpetrator(val apology: PreApology, val perpetrator: Perpetrator)

data class CreateVisionCommand(
    val perpetratorFirstName: String,
    val perpetratorLastName: String,
    val crimeType: CrimeType
)