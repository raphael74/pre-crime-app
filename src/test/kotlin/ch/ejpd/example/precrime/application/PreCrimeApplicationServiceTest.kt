package ch.ejpd.example.precrime.application

import ch.ejpd.example.precrime.domain.DomainEventPublisher
import ch.ejpd.example.precrime.domain.apology.*
import ch.ejpd.example.precrime.domain.perpetrator.Perpetrator
import ch.ejpd.example.precrime.domain.perpetrator.PerpetratorRepository
import ch.ejpd.example.precrime.domain.prearrest.PreArrestExecutedEvent
import ch.ejpd.example.precrime.domain.prearrest.PreArrestId
import ch.ejpd.example.precrime.domain.prearrest.PreArrestRepository
import ch.ejpd.example.precrime.domain.statistic.Statistic
import ch.ejpd.example.precrime.domain.statistic.StatisticRepository
import ch.ejpd.example.precrime.domain.vision.*
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
class PreCrimeApplicationServiceTest {

    private val visionRepository = mockk<VisionRepository>()
    private val statisticRepository = mockk<StatisticRepository>()
    private val preArrestRepository = mockk<PreArrestRepository>()
    private val preApologyRepository = mockk<PreApologyRepository>(relaxed = true)
    private val perpetratorRepository = mockk<PerpetratorRepository>()
    private val preEmptiveApologyDomainService = mockk<PreEmptiveApologyDomainService>()
    private val publisher = mockk<DomainEventPublisher>(relaxed = true)
    private val service = PreCrimeApplicationService(
        visionRepository,
        statisticRepository,
        preArrestRepository,
        preApologyRepository,
        perpetratorRepository,
        preEmptiveApologyDomainService,
        publisher
    )

    @Test
    fun `getPreventedCrimesCount should return count from singleton statistic`() {
        // GIVEN
        val statistic = mockk<Statistic>()
        every { statistic.totalCrimesPrevented } returns 42
        every { statisticRepository.findSingleton() } returns statistic

        // WHEN
        val count = service.getPreventedCrimesCount()

        // THEN
        assertThat(count).isEqualTo(42)
        verify { statisticRepository.findSingleton() }
    }

    @Test
    fun `triggerVision should find singleton, foresee crime and save`() {
        // GIVEN
        val perpetrator = Perpetrator(firstName = "John", lastName = "Doe")
        every { perpetratorRepository.findByFirstAndLastName("John", "Doe") } returns perpetrator

        val vision = mockk<Vision>()
        every { vision.id } returns VisionId()
        every { vision.foreseeCrime() } returns Unit
        mockkObject(VisionFactory.Companion)
        every { VisionFactory.createVision(any(), any(), any()) } returns vision
        every { visionRepository.create(any()) } returns Unit
        val cmd = CreateVisionCommand("John", "Doe", CrimeType.MURDER)

        // WHEN
        service.triggerVision(cmd)

        // THEN
        verify {
            perpetratorRepository.findByFirstAndLastName("John", "Doe")
            visionRepository.create(any())
            vision.foreseeCrime()
        }
    }

    @Test
    fun `onCrimeForeseen should save pre-arrest`() {
        // GIVEN
        val visionId = VisionId()
        val perpetrator = Perpetrator(firstName = "John", lastName = "Doe")
        val event = CrimeForeseenEvent(visionId, perpetrator.id, CrimeType.MURDER, LocalDateTime.now())
        every { preArrestRepository.save(any()) } returns Unit

        // WHEN
        service.onCrimeForeseen(event)

        // THEN
        verify {
            preArrestRepository.save(match {
                it.visionId == visionId && it.perpetratorId == perpetrator.id
            })
        }
    }

    @Test
    fun `onPreArrestExecuted should find singleton statistic, record prevention and save`() {
        // GIVEN
        val visionId = VisionId()
        val perpetrator = Perpetrator(firstName = "John", lastName = "Doe")
        val event = PreArrestExecutedEvent(PreArrestId(), visionId, perpetrator.id)

        val vision = Vision(
            id = visionId,
            perpetratorId = perpetrator.id,
            crimeType = CrimeType.MURDER,
            foreseenAt = LocalDateTime.now(),
            publisher = publisher
        )
        every { visionRepository.findById(any()) } returns vision

        val statistic = mockk<Statistic>(relaxed = true)
        every { statisticRepository.findSingleton() } returns statistic
        every { statisticRepository.update(any()) } returns Unit

        val apology = PreApology(
            visionId = visionId,
            perpetratorId = perpetrator.id,
            compensation = Compensation(10000.0, 450.0, 250.0, 9300.0),
            apologyLetter = ApologyLetter("Dear family..."),
            publisher = publisher
        ).apply { issue() }
        every { preEmptiveApologyDomainService.generateApology(vision) } returns apology
        every { preApologyRepository.save(any()) } returns Unit

        // WHEN
        service.onPreArrestExecuted(event)

        // THEN
        verify {
            statisticRepository.findSingleton()
            statistic.recordPrevention()
            statisticRepository.update(statistic)
            preEmptiveApologyDomainService.generateApology(vision)
            preApologyRepository.save(apology)
        }
    }
}
