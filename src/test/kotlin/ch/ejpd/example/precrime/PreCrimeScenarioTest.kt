package ch.ejpd.example.precrime

import ch.ejpd.example.precrime.application.PreCrimeApplicationService
import ch.ejpd.example.precrime.domain.precog.PrecogDivisionRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@TestPropertySource("/application-test.properties")
class PreCrimeScenarioTest {

    @Autowired
    private lateinit var applicationService: PreCrimeApplicationService

    @Autowired
    private lateinit var precogRepository: PrecogDivisionRepository

    @Test
    @Transactional
    fun `a future crime foreseen results in a pre-arrest and updated stats`() {
        // GIVEN: The department is operational
        val initialStats = precogRepository.findSingleton().totalCrimesPrevented

        // WHEN: Precogs foresee a crime
        val perpetrator = "John Anderton"
        val crimeType = "Future Murder"
        applicationService.triggerVision(perpetrator, crimeType)

        // THEN: The statistics should be updated via the bidirectional event flow
        val updatedStats = precogRepository.findSingleton().totalCrimesPrevented

        assertThat(updatedStats).isEqualTo(initialStats + 1)
    }
}
