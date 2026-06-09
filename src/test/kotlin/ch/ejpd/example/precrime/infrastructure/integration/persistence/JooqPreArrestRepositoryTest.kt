package ch.ejpd.example.precrime.infrastructure.integration.persistence

import ch.ejpd.example.precrime.IntegrationTest
import ch.ejpd.example.precrime.domain.perpetrator.Perpetrator
import ch.ejpd.example.precrime.domain.perpetrator.PerpetratorRepository
import ch.ejpd.example.precrime.domain.prearrest.PreArrest
import ch.ejpd.example.precrime.domain.prearrest.PreArrestId
import ch.ejpd.example.precrime.domain.prearrest.PreArrestStatus
import ch.ejpd.example.precrime.domain.vision.CrimeType
import ch.ejpd.example.precrime.domain.vision.Vision
import ch.ejpd.example.precrime.domain.vision.VisionRepository
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
class JooqPreArrestRepositoryTest(
    @Autowired private var repository: JooqPreArrestRepository,
    @Autowired private var perpetratorRepository: PerpetratorRepository,
    @Autowired private val visionRepository: VisionRepository
) {

    @Test
    fun `should save and find pre-arrest`() {
        // GIVEN
        val perpetrator = Perpetrator(firstName = "John", lastName = "Doe")
        perpetratorRepository.create(perpetrator)

        val vision = Vision(
            perpetratorId = perpetrator.id,
            crimeType = CrimeType.THEFT,
            foreseenAt = OffsetDateTime.now()
        )
        visionRepository.create(vision)

        val preArrestDate = OffsetDateTime.now()
        val preArrest = PreArrest(
            visionId = vision.id,
            perpetratorId = perpetrator.id,
            preArrestDate = preArrestDate
        )

        // WHEN
        repository.create(preArrest)

        // THEN
        val result = repository.findById(preArrest.id)
        assertThat(result).isNotNull
        assertThat(result!!.id).isEqualTo(preArrest.id)
        assertThat(result.visionId).isEqualTo(vision.id)
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

        val vision1 = Vision(
            perpetratorId = p1.id,
            crimeType = CrimeType.THEFT,
            foreseenAt = OffsetDateTime.now()
        )
        val vision2 = Vision(
            perpetratorId = p2.id,
            crimeType = CrimeType.THEFT,
            foreseenAt = OffsetDateTime.now()
        )
        visionRepository.create(vision1)
        visionRepository.create(vision2)

        val preArrest1 = PreArrest(
            visionId = vision1.id,
            perpetratorId = p1.id,
            preArrestDate = OffsetDateTime.now().minusDays(1),
            status = PreArrestStatus.ARRESTED_BEFORE_CRIME
        )
        val preArrest2 = PreArrest(
            visionId = vision2.id,
            perpetratorId = p2.id,
            preArrestDate = OffsetDateTime.now(),
            status = PreArrestStatus.ARRESTED_BEFORE_CRIME
        )
        repository.create(preArrest1)
        repository.create(preArrest2)

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
