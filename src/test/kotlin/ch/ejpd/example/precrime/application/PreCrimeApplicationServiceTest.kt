package ch.ejpd.example.precrime.application

import ch.ejpd.example.precrime.domain.DomainEventPublisher
import ch.ejpd.example.precrime.domain.apology.*
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
    private val preEmptiveApologyDomainService = mockk<PreEmptiveApologyDomainService>()
    private val publisher = mockk<DomainEventPublisher>(relaxed = true)
    private val service = PreCrimeApplicationService(
        visionRepository,
        statisticRepository,
        preArrestRepository,
        preApologyRepository,
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
    fun `triggerVision should find singleton, foresee crime and save and publish`() {
        // GIVEN
        val vision = mockk<Vision>()
        every { vision.id } returns VisionId()
        every { vision.foreseeCrime() } returns Unit
        every { vision.domainEvents } returns emptyList()
        every { vision.clearDomainEvents() } returns Unit
        mockkObject(VisionFactory.Companion)
        every { VisionFactory.createVision(any(), any()) } returns vision
        every { visionRepository.create(any()) } returns Unit

        // WHEN
        service.triggerVision("John", "Doe", CrimeType.MURDER)

        // THEN
        verify {
            visionRepository.create(any())
            vision.foreseeCrime()
            publisher.publish(any())
            vision.clearDomainEvents()
        }
    }

    @Test
    fun `onCrimeForeseen should save pre-arrest and publish event`() {
        // GIVEN
        val visionId = VisionId()
        val event = CrimeForeseenEvent(visionId, Perpetrator("John", "Doe"), CrimeType.MURDER, LocalDateTime.now())
        every { preArrestRepository.save(any()) } returns Unit

        // WHEN
        service.onCrimeForeseen(event)

        // THEN
        verify {
            preArrestRepository.save(match {
                it.visionId == visionId && it.perpetrator == Perpetrator("John", "Doe")
            })
            publisher.publish(any())
        }
    }

    @Test
    fun `onPreArrestExecuted should find singleton statistic, record prevention and save and publish`() {
        // GIVEN
        val visionId = VisionId()
        val event = PreArrestExecutedEvent(PreArrestId(), visionId, Perpetrator("John", "Doe"))

        val vision = Vision(
            id = visionId,
            perpetrator = Perpetrator("John", "Doe"),
            crimeType = CrimeType.MURDER,
            foreseenAt = LocalDateTime.now()
        )
        every { visionRepository.findById(any()) } returns vision

        val statistic = mockk<Statistic>(relaxed = true)
        every { statisticRepository.findSingleton() } returns statistic
        every { statisticRepository.update(any()) } returns Unit

        val apology = PreApology(
            visionId = visionId,
            perpetrator = Perpetrator("John", "Doe"),
            compensation = Compensation(10000.0, 450.0, 250.0, 9300.0),
            apologyLetter = ApologyLetter("Dear family...")
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
            publisher.publish(any())
            apology.clearDomainEvents()
        }
    }
}
