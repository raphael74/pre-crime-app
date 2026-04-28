package ch.ejpd.example.precrime.application

import ch.ejpd.example.precrime.domain.enforcement.LawEnforcementRepository
import ch.ejpd.example.precrime.domain.enforcement.LawEnforcementUnit
import ch.ejpd.example.precrime.domain.enforcement.PreArrestExecutedEvent
import ch.ejpd.example.precrime.domain.enforcement.PreArrestId
import ch.ejpd.example.precrime.domain.precog.*
import ch.ejpd.example.precrime.infrastructure.integration.persistence.JooqInboxRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

class PreCrimeApplicationServiceTest {

    private val precogRepository = mockk<PrecogDivisionRepository>()
    private val enforcementRepository = mockk<LawEnforcementRepository>()
    private val inboxRepository = mockk<JooqInboxRepository>(relaxed = true)
    private val service = PreCrimeApplicationService(precogRepository, enforcementRepository, inboxRepository)

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
    fun `triggerVision should find singleton, foresee crime and save`() {
        // GIVEN
        val division = mockk<PrecogDivision>(relaxed = true)
        every { precogRepository.findSingleton() } returns division
        every { precogRepository.save(any()) } returns Unit

        // WHEN
        service.triggerVision("John Doe", "Murder")

        // THEN
        verify {
            precogRepository.findSingleton()
            division.foreseeCrime(Perpetrator("John Doe"), CrimeType("Murder"))
            precogRepository.save(division)
        }
    }

    @Test
    fun `onCrimeForeseen should find singleton enforcement unit, execute pre-arrest and save`() {
        // GIVEN
        val visionId = VisionId()
        val event = CrimeForeseenEvent(visionId, Perpetrator("John Doe"), CrimeType("Murder"), LocalDateTime.now())
        val unit = mockk<LawEnforcementUnit>(relaxed = true)
        every { enforcementRepository.findSingleton() } returns unit
        every { enforcementRepository.save(any()) } returns Unit
        every { inboxRepository.insertIfNotExists(any(), any()) } returns true

        // WHEN
        service.onCrimeForeseen(event, UUID.randomUUID().toString(), "some-group")

        // THEN
        verify {
            enforcementRepository.findSingleton()
            unit.executePreArrest(visionId, Perpetrator("John Doe"))
            enforcementRepository.save(unit)
        }
    }

    @Test
    fun `onPreArrestExecuted should find singleton precog division, record prevention and save`() {
        // GIVEN
        val event = PreArrestExecutedEvent(PreArrestId(), VisionId(), Perpetrator("John Doe"))
        val division = mockk<PrecogDivision>(relaxed = true)
        every { precogRepository.findSingleton() } returns division
        every { precogRepository.save(any()) } returns Unit
        every { inboxRepository.insertIfNotExists(any(), any()) } returns true

        // WHEN
        service.onPreArrestExecuted(event, UUID.randomUUID().toString(), "some-group")

        // THEN
        verify {
            precogRepository.findSingleton()
            division.recordPrevention()
            precogRepository.save(division)
        }
    }
}
