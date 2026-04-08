package ch.ejpd.example.precrime.infrastructure.integration.persistence

import ch.ejpd.example.precrime.IntegrationTest
import ch.ejpd.example.precrime.domain.precog.PrecogDivisionId
import ch.ejpd.example.precrime.domain.precog.Vision
import ch.ejpd.example.precrime.domain.precog.VisionId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

@IntegrationTest
@Transactional
class JooqPrecogDivisionRepositoryTest {

    @Autowired
    private lateinit var repository: JooqPrecogDivisionRepository

    private val SINGLETON_ID = PrecogDivisionId(UUID.fromString("00000000-0000-0000-0000-000000000001"))

    @Test
    fun `should find singleton division`() {
        // WHEN
        val division = repository.findSingleton()

        // THEN
        assertThat(division).isNotNull
        assertThat(division.id).isEqualTo(SINGLETON_ID)
    }

    @Test
    fun `should persist total crimes prevented`() {
        // GIVEN
        val division = repository.findSingleton()
        division.recordPrevention()
        division.recordPrevention()

        // WHEN
        repository.save(division)

        // THEN
        val result = repository.findSingleton()
        assertThat(result.totalCrimesPrevented).isEqualTo(2)
    }

    @Test
    fun `should persist visions in collection`() {
        // GIVEN
        val division = repository.findSingleton()
        val foreseenAt = LocalDateTime.now().plusHours(1).truncatedTo(ChronoUnit.SECONDS)
        val vision = Vision(VisionId(), "Alice Smith", "Theft", foreseenAt)
        division.visions.add(vision)

        // WHEN
        repository.save(division)

        // THEN
        val result = repository.findSingleton()
        assertThat(result.visions).hasSize(1)
        val persistedVision = result.visions.first()
        assertThat(persistedVision.perpetrator).isEqualTo("Alice Smith")
        assertThat(persistedVision.crimeType).isEqualTo("Theft")
        // Note: database might truncate fractional seconds if not configured, 
        // using truncatedTo(SECONDS) to avoid mismatch.
        assertThat(persistedVision.foreseenAt.truncatedTo(ChronoUnit.SECONDS))
            .isEqualTo(foreseenAt)
    }

    @Test
    fun `should return null when finding by non-existing id`() {
        // GIVEN
        val randomId = PrecogDivisionId(UUID.randomUUID())

        // WHEN
        val result = repository.findById(randomId)

        // THEN
        assertThat(result).isNull()
    }
}
