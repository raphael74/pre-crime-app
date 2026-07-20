package org.example.precrime.infrastructure.integration.persistence

import org.example.precrime.IntegrationTest
import org.example.precrime.domain.AggregateVersion
import org.example.precrime.domain.perpetrator.Perpetrator
import org.example.precrime.domain.perpetrator.PerpetratorRepository
import org.example.precrime.domain.vision.CrimeType
import org.example.precrime.domain.vision.Vision
import org.example.precrime.domain.vision.VisionId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.*

@IntegrationTest
@Transactional
class JooqVisionRepositoryTest(
    @Autowired private var repository: JooqVisionRepository,
    @Autowired private var perpetratorRepository: PerpetratorRepository
) {

    @Test
    fun `should save and find vision`() {
        // GIVEN
        val perpetrator = Perpetrator(firstName = "John", lastName = "Doe")
        perpetratorRepository.create(perpetrator)

        val vision = Vision(
            perpetratorId = perpetrator.id,
            crimeType = CrimeType.MURDER,
            foreseenAt = OffsetDateTime.now()
        )

        // WHEN
        repository.create(vision)

        // THEN
        val result = repository.findById(vision.id)
        assertThat(result).isNotNull
        assertThat(result!!.id).isEqualTo(vision.id)
        assertThat(result.perpetratorId).isEqualTo(perpetrator.id)
        assertThat(result.crimeType).isEqualTo(CrimeType.MURDER)
        assertThat(result.foreseenAt).isNotNull()
    }

    @Test
    fun `should return null when finding by non-existing id`() {
        // GIVEN
        val randomId = VisionId(UUID.randomUUID())

        // WHEN
        val result = repository.findById(randomId)

        // THEN
        assertThat(result).isNull()
    }

    @Test
    fun `should update vision and increment version`() {
        // GIVEN
        val perpetrator = Perpetrator(firstName = "John", lastName = "Doe")
        perpetratorRepository.create(perpetrator)

        val vision = Vision(
            perpetratorId = perpetrator.id,
            crimeType = CrimeType.MURDER,
            foreseenAt = OffsetDateTime.now()
        )
        repository.create(vision)

        val initialVersion = vision.version

        // WHEN
        repository.update(vision)

        // THEN
        val updated = repository.findById(vision.id)
        assertThat(updated).isNotNull
        assertThat(updated!!.version).isNotEqualTo(initialVersion)
    }

    @Test
    fun `should throw optimistic locking exception on stale update`() {
        // GIVEN
        val perpetrator = Perpetrator(firstName = "John", lastName = "Doe")
        perpetratorRepository.create(perpetrator)

        val vision = Vision(
            perpetratorId = perpetrator.id,
            crimeType = CrimeType.MURDER,
            foreseenAt = OffsetDateTime.now()
        )
        repository.create(vision)
        repository.update(vision)

        // WHEN - try to update with stale version
        val stale = Vision(
            id = vision.id,
            version = AggregateVersion(0),
            perpetratorId = perpetrator.id,
            crimeType = CrimeType.MURDER,
            foreseenAt = OffsetDateTime.now()
        )

        // THEN
        org.junit.jupiter.api.Assertions.assertThrows(OptimisticLockingException::class.java) {
            repository.update(stale)
        }
    }
}
