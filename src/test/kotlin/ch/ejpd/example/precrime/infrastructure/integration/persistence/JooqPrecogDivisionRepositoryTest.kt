package ch.ejpd.example.precrime.infrastructure.integration.persistence

import ch.ejpd.example.precrime.IntegrationTest
import ch.ejpd.example.precrime.domain.precog.CrimeType
import ch.ejpd.example.precrime.domain.precog.Perpetrator
import ch.ejpd.example.precrime.domain.precog.PrecogDivisionId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
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
        repository.update(division)

        // THEN
        val result = repository.findSingleton()
        assertThat(result.totalCrimesPrevented).isEqualTo(2)
    }

    @Test
    fun `should persist visions in collection`() {
        // GIVEN
        val division = repository.findSingleton()
        val perpetrator = Perpetrator("Alice Smith")
        val crimeType = CrimeType.THEFT

        // WHEN
        val visionId = division.foreseeCrime(perpetrator, crimeType)
        repository.update(division)

        // THEN
        val result = repository.findSingleton()
        assertThat(result.visions).hasSize(1)
        val persistedVision = result.visions.first()
        assertThat(persistedVision.id).isEqualTo(visionId)
        assertThat(persistedVision.perpetrator).isEqualTo(perpetrator)
        assertThat(persistedVision.crimeType).isEqualTo(crimeType)
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
