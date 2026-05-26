package ch.ejpd.example.precrime.domain.precog

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PrecogDivisionTest {

    private val precogDivision = PrecogDivision()

    @Test
    fun `foreseeCrime should record CrimeForeseenEvent`() {
        // GIVEN
        val perpetrator = Perpetrator("John Doe")
        val crimeType = CrimeType.MURDER

        // WHEN
        val visionId = precogDivision.foreseeCrime(perpetrator, crimeType)

        // THEN
        assertThat(visionId).isNotNull
        assertThat(precogDivision.domainEvents).hasSize(1)
        val event = precogDivision.domainEvents.first() as CrimeForeseenEvent
        assertThat(event.visionId).isEqualTo(visionId)
        assertThat(event.perpetrator).isEqualTo(perpetrator)
        assertThat(event.crimeType).isEqualTo(crimeType)
    }

    @Test
    fun `recordPrevention should increment totalCrimesPrevented`() {
        // GIVEN
        val initialCount = precogDivision.totalCrimesPrevented

        // WHEN
        precogDivision.recordPrevention()

        // THEN
        assertThat(precogDivision.totalCrimesPrevented).isEqualTo(initialCount + 1)
    }
}
