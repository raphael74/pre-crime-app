package ch.ejpd.example.precrime.domain.precog

import ch.ejpd.example.precrime.domain.DomainEventPublisher
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PrecogDivisionTest {

    private val publisher = mockk<DomainEventPublisher>(relaxed = true)
    private val precogDivision = PrecogDivision()

    @Test
    fun `foreseeCrime should publish CrimeForeseenEvent`() {
        // GIVEN
        precogDivision.register(publisher)
        val perpetrator = "John Doe"
        val crimeType = "Murder"

        // WHEN
        val visionId = precogDivision.foreseeCrime(perpetrator, crimeType)

        // THEN
        assertThat(visionId).isNotNull
        verify {
            publisher.publish(match {
                it is CrimeForeseenEvent &&
                        it.visionId == visionId &&
                        it.perpetrator == perpetrator &&
                        it.crimeType == crimeType
            })
        }
        confirmVerified(publisher)
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
