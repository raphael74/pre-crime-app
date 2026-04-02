package ch.ejpd.example.precrime

import ch.ejpd.example.precrime.application.PreCrimeApplicationService
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.Rollback
import java.util.concurrent.TimeUnit

@IntegrationTest
class PreCrimeScenarioTest {

    @Autowired
    private lateinit var applicationService: PreCrimeApplicationService

    @Test
    @Rollback(false)
    fun `a future crime foreseen results in a pre-arrest and updated stats`() {
        // GIVEN: The department is operational
        val initialCrimeCount = applicationService.getPreventedCrimesCount()

        // WHEN: Precogs foresee a crime
        val perpetrator = "John Doe"
        val crimeType = "Future Murder"
        applicationService.triggerVision(perpetrator, crimeType)

        // THEN: The statistics should be updated via the bidirectional event flow
        await().pollInterval(1, TimeUnit.SECONDS).atMost(10, TimeUnit.SECONDS).untilAsserted {
            val updatedCrimeCount = applicationService.getPreventedCrimesCount()
            assertThat(updatedCrimeCount).isEqualTo(initialCrimeCount + 1)
        }
    }
}
