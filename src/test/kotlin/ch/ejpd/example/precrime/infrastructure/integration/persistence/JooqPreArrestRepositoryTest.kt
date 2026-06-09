package ch.ejpd.example.precrime.infrastructure.integration.persistence

import ch.ejpd.example.precrime.IntegrationTest
import ch.ejpd.example.precrime.domain.perpetrator.Perpetrator
import ch.ejpd.example.precrime.domain.perpetrator.PerpetratorRepository
import ch.ejpd.example.precrime.domain.prearrest.PreArrest
import ch.ejpd.example.precrime.domain.prearrest.PreArrestId
import ch.ejpd.example.precrime.domain.prearrest.PreArrestStatus
import ch.ejpd.example.precrime.domain.vision.VisionId
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
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
        perpetratorRepository.create(perpetrator)
        val preArrestDate = OffsetDateTime.now()
        val preArrest = PreArrest(
            visionId = visionId,
            perpetratorId = perpetrator.id,
            preArrestDate = preArrestDate
        )

        // WHEN
        repository.save(preArrest)

        // THEN
        val result = repository.findById(preArrest.id)
        assertThat(result).isNotNull
        assertThat(result!!.id).isEqualTo(preArrest.id)
        assertThat(result.visionId).isEqualTo(visionId)
        assertThat(result.perpetratorId).isEqualTo(perpetrator.id)
        assertThat(result.preArrestDate).isCloseTo(preArrestDate, within(1, ChronoUnit.SECONDS))
        assertThat(result.status).isEqualTo(PreArrestStatus.PENDING)
    }

    @Test
    fun `should find all pre-arrests sorted by date descending`() {
        // GIVEN
        val p1 = Perpetrator(firstName = "John", lastName = "Doe")
        val p2 = Perpetrator(firstName = "Jane", lastName = "Smith")
        perpetratorRepository.create(p1)
        perpetratorRepository.create(p2)

        val preArrest1 = PreArrest(
            visionId = VisionId(),
            perpetratorId = p1.id,
            preArrestDate = OffsetDateTime.now().minusDays(1),
            status = PreArrestStatus.ARRESTED_BEFORE_CRIME
        )
        val preArrest2 = PreArrest(
            visionId = VisionId(),
            perpetratorId = p2.id,
            preArrestDate = OffsetDateTime.now(),
            status = PreArrestStatus.ARRESTED_BEFORE_CRIME
        )
        repository.save(preArrest1)
        repository.save(preArrest2)

        // WHEN
        val results = repository.findAllArrested()

        // THEN
        assertThat(results).hasSize(2)
        assertThat(results[0].id).isEqualTo(preArrest2.id)
        assertThat(results[1].id).isEqualTo(preArrest1.id)
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
