package ch.ejpd.example.precrime

import ch.ejpd.example.precrime.application.PreCrimeApplicationService
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestPropertySource
import java.util.concurrent.TimeUnit

@SpringBootTest
@EmbeddedKafka(partitions = 1)
@TestPropertySource("/application-test.properties")
@DirtiesContext
class PreCrimeScenarioTest {

    @Autowired
    private lateinit var applicationService: PreCrimeApplicationService

    @Test
    fun `a future crime foreseen results in a pre-arrest and updated stats`() {
        // GIVEN: The department is operational
        val initialCrimeCount = applicationService.getPreventedCrimesCount()

        // WHEN: Precogs foresee a crime
        val perpetrator = "John Anderton"
        val crimeType = "Future Murder"
        applicationService.triggerVision(perpetrator, crimeType)

        // THEN: The statistics should be updated via the bidirectional event flow
        await().atMost(20, TimeUnit.SECONDS).untilAsserted {
            val updatedCrimeCount = applicationService.getPreventedCrimesCount()
            assertThat(updatedCrimeCount).isEqualTo(initialCrimeCount + 1)
        }
    }
}
