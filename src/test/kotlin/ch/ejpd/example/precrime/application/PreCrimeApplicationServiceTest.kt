package ch.ejpd.example.precrime.application

import ch.ejpd.example.precrime.domain.DomainEventPublisher
import ch.ejpd.example.precrime.domain.apology.*
import ch.ejpd.example.precrime.domain.enforcement.LawEnforcementRepository
import ch.ejpd.example.precrime.domain.enforcement.LawEnforcementUnit
import ch.ejpd.example.precrime.domain.enforcement.PreArrestExecutedEvent
import ch.ejpd.example.precrime.domain.enforcement.PreArrestId
import ch.ejpd.example.precrime.domain.precog.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class PreCrimeApplicationServiceTest {

    private val precogRepository = mockk<PrecogDivisionRepository>()
    private val enforcementRepository = mockk<LawEnforcementRepository>()
    private val preApologyRepository = mockk<PreApologyRepository>(relaxed = true)
    private val preEmptiveApologyDomainService = mockk<PreEmptiveApologyDomainService>()
    private val publisher = mockk<DomainEventPublisher>(relaxed = true)
    private val service = PreCrimeApplicationService(
        precogRepository,
        enforcementRepository,
        preApologyRepository,
        preEmptiveApologyDomainService,
        publisher
    )

    @Test
    fun `getPreventedCrimesCount should return count from singleton precog division`() {
        // GIVEN
        val division = mockk<PrecogDivision>()
        every { division.totalCrimesPrevented } returns 42
        every { precogRepository.findSingleton() } returns division

        // WHEN
        val count = service.getPreventedCrimesCount()

        // THEN
        assertThat(count).isEqualTo(42)
        verify { precogRepository.findSingleton() }
    }

    @Test
    fun `triggerVision should find singleton, foresee crime and save and publish`() {
        // GIVEN
        val division = mockk<PrecogDivision>(relaxed = true)
        every { precogRepository.findSingleton() } returns division
        every { precogRepository.update(any()) } returns Unit

        // WHEN
        service.triggerVision("John Doe", CrimeType.MURDER)

        // THEN
        verify {
            precogRepository.findSingleton()
            division.foreseeCrime(Perpetrator("John Doe"), CrimeType.MURDER)
            precogRepository.update(division)
            publisher.publish(any())
            division.clearDomainEvents()
        }
    }

    @Test
    fun `onCrimeForeseen should find singleton enforcement unit, execute pre-arrest and save and publish`() {
        // GIVEN
        val visionId = VisionId()
        val event = CrimeForeseenEvent(visionId, Perpetrator("John Doe"), CrimeType.MURDER, LocalDateTime.now())
        val unit = mockk<LawEnforcementUnit>(relaxed = true)
        every { enforcementRepository.findSingleton() } returns unit
        every { enforcementRepository.update(any()) } returns Unit

        // WHEN
        service.onCrimeForeseen(event)

        // THEN
        verify {
            enforcementRepository.findSingleton()
            unit.executePreArrest(visionId, Perpetrator("John Doe"))
            enforcementRepository.update(unit)
            publisher.publish(any())
            unit.clearDomainEvents()
        }
    }

    @Test
    fun `onPreArrestExecuted should find singleton precog division, record prevention and save and publish`() {
        // GIVEN
        val visionId = VisionId()
        val event = PreArrestExecutedEvent(PreArrestId(), visionId, Perpetrator("John Doe"))

        val vision = Vision(visionId, Perpetrator("John Doe"), CrimeType.MURDER, LocalDateTime.now())
        val division = mockk<PrecogDivision>(relaxed = true)
        every { division.visions } returns setOf(vision)
        every { precogRepository.findSingleton() } returns division
        every { precogRepository.update(any()) } returns Unit

        val unit = mockk<LawEnforcementUnit>()
        every { enforcementRepository.findSingleton() } returns unit

        val apology = PreApology(
            visionId = visionId,
            perpetrator = Perpetrator("John Doe"),
            compensation = Compensation(10000.0, 450.0, 250.0, 9300.0),
            apologyLetter = ApologyLetter("Dear family...")
        ).apply { issue() }
        every { preEmptiveApologyDomainService.generateApology(vision) } returns apology
        every { preApologyRepository.save(any()) } returns Unit

        // WHEN
        service.onPreArrestExecuted(event)

        // THEN
        verify {
            precogRepository.findSingleton()
            division.recordPrevention()
            precogRepository.update(division)
            preEmptiveApologyDomainService.generateApology(vision)
            preApologyRepository.save(apology)
            publisher.publish(any())
            division.clearDomainEvents()
        }
    }
}
