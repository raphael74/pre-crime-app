package ch.ejpd.example.precrime.infrastructure.integration.persistence

import ch.ejpd.example.precrime.IntegrationTest
import ch.ejpd.example.precrime.domain.perpetrator.Perpetrator
import ch.ejpd.example.precrime.domain.perpetrator.PerpetratorRepository
import ch.ejpd.example.precrime.domain.prearrest.PreArrest
import ch.ejpd.example.precrime.domain.prearrest.PreArrestId
import ch.ejpd.example.precrime.domain.prearrest.PreArrestStatus
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

    @Autowired
    private lateinit var perpetratorRepository: PerpetratorRepository

    @Test
    fun `should save and find pre-arrest`() {
        // GIVEN
        val visionId = VisionId()
        val perpetrator = Perpetrator(firstName = "John", lastName = "Doe")
        perpetratorRepository.save(perpetrator)
        val preArrest = PreArrest(visionId = visionId, perpetratorId = perpetrator.id)

        // WHEN
        repository.save(preArrest)

        // THEN
        val result = repository.findById(preArrest.id)
        assertThat(result).isNotNull
        assertThat(result!!.id).isEqualTo(preArrest.id)
        assertThat(result.visionId).isEqualTo(visionId)
        assertThat(result.perpetratorId).isEqualTo(perpetrator.id)
        assertThat(result.status).isEqualTo(PreArrestStatus.ARRESTED_BEFORE_CRIME)
    }

    @Test
    fun `should find all pre-arrests`() {
        // GIVEN
        val p1 = Perpetrator(firstName = "John", lastName = "Doe")
        val p2 = Perpetrator(firstName = "Jane", lastName = "Smith")
        perpetratorRepository.save(p1)
        perpetratorRepository.save(p2)

        val preArrest1 = PreArrest(visionId = VisionId(), perpetratorId = p1.id)
        val preArrest2 = PreArrest(visionId = VisionId(), perpetratorId = p2.id)
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
