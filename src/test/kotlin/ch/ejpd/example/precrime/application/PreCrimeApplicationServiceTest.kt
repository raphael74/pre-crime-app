package ch.ejpd.example.precrime.application

import ch.ejpd.example.precrime.domain.enforcement.LawEnforcementRepository
import ch.ejpd.example.precrime.domain.enforcement.LawEnforcementUnit
import ch.ejpd.example.precrime.domain.enforcement.PreArrestExecutedEvent
import ch.ejpd.example.precrime.domain.enforcement.PreArrestId
import ch.ejpd.example.precrime.domain.precog.CrimeForeseenEvent
import ch.ejpd.example.precrime.domain.precog.PrecogDivision
import ch.ejpd.example.precrime.domain.precog.PrecogDivisionRepository
import ch.ejpd.example.precrime.domain.precog.VisionId
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PreCrimeApplicationServiceTest {

    private val precogRepository = mockk<PrecogDivisionRepository>()
    private val enforcementRepository = mockk<LawEnforcementRepository>()
    private val service = PreCrimeApplicationService(precogRepository, enforcementRepository)

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
            division.foreseeCrime("John Doe", "Murder")
            precogRepository.save(division)
        }
    }

    @Test
    fun `onCrimeForeseen should find singleton enforcement unit, execute pre-arrest and save`() {
        // GIVEN
        val visionId = VisionId()
        val event = CrimeForeseenEvent(visionId, "John Doe", "Murder", mockk())
        val unit = mockk<LawEnforcementUnit>(relaxed = true)
        every { enforcementRepository.findSingleton() } returns unit
        every { enforcementRepository.save(any()) } returns Unit

        // WHEN
        service.onCrimeForeseen(event)

        // THEN
        verify {
            enforcementRepository.findSingleton()
            unit.executePreArrest(visionId, "John Doe")
            enforcementRepository.save(unit)
        }
    }

    @Test
    fun `onPreArrestExecuted should find singleton precog division, record prevention and save`() {
        // GIVEN
        val event = PreArrestExecutedEvent(PreArrestId(), VisionId(), "John Doe")
        val division = mockk<PrecogDivision>(relaxed = true)
        every { precogRepository.findSingleton() } returns division
        every { precogRepository.save(any()) } returns Unit

        // WHEN
        service.onPreArrestExecuted(event)

        // THEN
        verify {
            precogRepository.findSingleton()
            division.recordPrevention()
            precogRepository.save(division)
        }
    }
}
