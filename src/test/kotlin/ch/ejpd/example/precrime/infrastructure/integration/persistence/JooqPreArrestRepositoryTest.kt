package ch.ejpd.example.precrime.infrastructure.integration.persistence

import ch.ejpd.example.precrime.IntegrationTest
import ch.ejpd.example.precrime.domain.enforcement.PreArrest
import ch.ejpd.example.precrime.domain.enforcement.PreArrestId
import ch.ejpd.example.precrime.domain.enforcement.PreArrestStatus
import ch.ejpd.example.precrime.domain.vision.Perpetrator
import ch.ejpd.example.precrime.domain.vision.VisionId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.util.*

@IntegrationTest
@Transactional
class JooqPreArrestRepositoryTest {

    @Autowired
    private lateinit var repository: JooqPreArrestRepository

    @Test
    fun `should save and find pre-arrest`() {
        // GIVEN
        val visionId = VisionId()
        val perpetrator = Perpetrator("John", "Doe")
        val preArrest = PreArrest(visionId = visionId, perpetrator = perpetrator)

        // WHEN
        repository.save(preArrest)

        // THEN
        val result = repository.findById(preArrest.id)
        assertThat(result).isNotNull
        assertThat(result!!.id).isEqualTo(preArrest.id)
        assertThat(result.visionId).isEqualTo(visionId)
        assertThat(result.perpetrator).isEqualTo(perpetrator)
        assertThat(result.status).isEqualTo(PreArrestStatus.ARRESTED_BEFORE_CRIME)
    }

    @Test
    fun `should find all pre-arrests`() {
        // GIVEN
        val preArrest1 = PreArrest(visionId = VisionId(), perpetrator = Perpetrator("John", "Doe"))
        val preArrest2 = PreArrest(visionId = VisionId(), perpetrator = Perpetrator("Jane", "Smith"))
        repository.save(preArrest1)
        repository.save(preArrest2)

        // WHEN
        val results = repository.findAll()

        // THEN
        assertThat(results).hasSize(2)
        assertThat(results.map { it.id }).containsExactlyInAnyOrder(preArrest1.id, preArrest2.id)
    }

    @Test
    fun `should return null when finding by non-existing id`() {
        // GIVEN
        val randomId = PreArrestId(UUID.randomUUID())

        // WHEN
        val result = repository.findById(randomId)

        // THEN
        assertThat(result).isNull()
    }
}
