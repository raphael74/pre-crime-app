package org.example.precrime.infrastructure.integration.persistence

import org.example.precrime.IntegrationTest
import org.example.precrime.domain.perpetrator.Perpetrator
import org.example.precrime.domain.perpetrator.PerpetratorRepository
import org.example.precrime.domain.preapology.ApologyLetter
import org.example.precrime.domain.preapology.Compensation
import org.example.precrime.domain.preapology.PreApology
import org.example.precrime.domain.preapology.PreApologyId
import org.example.precrime.domain.prearrest.PreArrest
import org.example.precrime.domain.prearrest.PreArrestRepository
import org.example.precrime.domain.vision.CrimeType
import org.example.precrime.domain.vision.Vision
import org.example.precrime.domain.vision.VisionRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.*

@IntegrationTest
@Transactional
class JooqPreApologyRepositoryTest(
    @Autowired private var repository: JooqPreApologyRepository,
    @Autowired private var perpetratorRepository: PerpetratorRepository,
    @Autowired private val preArrestRepository: PreArrestRepository,
    @Autowired private val visionRepository: VisionRepository
) {

    @Test
    fun `should persist and retrieve pre-emptive apology`() {
        // GIVEN
        val apologyId = PreApologyId()
        val perpetrator = Perpetrator(firstName = "Danny", lastName = "Witwer")
        perpetratorRepository.create(perpetrator)

        val vision = Vision(
            perpetratorId = perpetrator.id,
            crimeType = CrimeType.THEFT,
            foreseenAt = OffsetDateTime.now()
        )
        visionRepository.create(vision)

        val preArrest = PreArrest(
            visionId = vision.id,
            perpetratorId = perpetrator.id,
            preArrestDate = OffsetDateTime.now()
        )
        preArrestRepository.create(preArrest)

        val compensation = Compensation(10000.0, 450.0, 250.0, 9300.0)
        val apologyLetter = ApologyLetter("Dear Family, we are sorry.")
        val preApology =
            PreApology(apologyId, preArrest.id, perpetrator.id, compensation, apologyLetter)

        // WHEN
        repository.create(preApology)

        // THEN
        val retrieved = repository.findById(apologyId)
        assertThat(retrieved).isNotNull
        assertThat(retrieved!!.id).isEqualTo(apologyId)
        assertThat(retrieved.preArrestId).isEqualTo(preArrest.id)
        assertThat(retrieved.perpetratorId).isEqualTo(perpetrator.id)
        assertThat(retrieved.compensation.baseAmount).isEqualTo(10000.0)
        assertThat(retrieved.compensation.netPayout).isEqualTo(9300.0)
        assertThat(retrieved.apologyLetter.text).isEqualTo("Dear Family, we are sorry.")
        assertThat(retrieved.createdAt).isNotNull()
    }

    @Test
    fun `should retrieve all persisted apologies sorted by createdAt descending`() {
        // GIVEN
        val p1 = Perpetrator(firstName = "Unknown", lastName = "Oldest")
        val p2 = Perpetrator(firstName = "Unknown", lastName = "Newest")
        val p3 = Perpetrator(firstName = "Unknown", lastName = "Middle")
        perpetratorRepository.create(p1)
        perpetratorRepository.create(p2)
        perpetratorRepository.create(p3)

        val v1 = Vision(perpetratorId = p1.id, crimeType = CrimeType.THEFT, foreseenAt = OffsetDateTime.now())
        val v2 = Vision(perpetratorId = p2.id, crimeType = CrimeType.THEFT, foreseenAt = OffsetDateTime.now())
        val v3 = Vision(perpetratorId = p3.id, crimeType = CrimeType.THEFT, foreseenAt = OffsetDateTime.now())
        visionRepository.create(v1)
        visionRepository.create(v2)
        visionRepository.create(v3)

        val pa1 = PreArrest(visionId = v1.id, perpetratorId = p1.id, preArrestDate = OffsetDateTime.now())
        val pa2 = PreArrest(visionId = v2.id, perpetratorId = p2.id, preArrestDate = OffsetDateTime.now())
        val pa3 = PreArrest(visionId = v3.id, perpetratorId = p3.id, preArrestDate = OffsetDateTime.now())
        preArrestRepository.create(pa1)
        preArrestRepository.create(pa2)
        preArrestRepository.create(pa3)

        val now = OffsetDateTime.now()
        val apology1 = PreApology(
            id = PreApologyId(),
            preArrestId = pa1.id,
            perpetratorId = p1.id,
            compensation = Compensation(100.0, 0.0, 0.0, 100.0),
            apologyLetter = ApologyLetter("Sorry 1"),
            createdAt = now.minusDays(2)
        )
        val apology2 = PreApology(
            id = PreApologyId(),
            preArrestId = pa2.id,
            perpetratorId = p2.id,
            compensation = Compensation(100.0, 0.0, 0.0, 100.0),
            apologyLetter = ApologyLetter("Sorry 2"),
            createdAt = now
        )
        val apology3 = PreApology(
            id = PreApologyId(),
            preArrestId = pa3.id,
            perpetratorId = p3.id,
            compensation = Compensation(100.0, 0.0, 0.0, 100.0),
            apologyLetter = ApologyLetter("Sorry 3"),
            createdAt = now.minusDays(1)
        )

        repository.create(apology1)
        repository.create(apology2)
        repository.create(apology3)

        // WHEN
        val all = repository.findAll()

        // THEN
        assertThat(all).hasSize(3)
        assertThat(all.map { it.perpetratorId }).containsExactly(p2.id, p3.id, p1.id)
    }

    @Test
    fun `should retrieve all persisted apologies`() {
        // GIVEN
        val p1 = Perpetrator(firstName = "Danny", lastName = "Witwer")
        val p2 = Perpetrator(firstName = "Arthur", lastName = "Pendelton")
        perpetratorRepository.create(p1)
        perpetratorRepository.create(p2)

        val v1 = Vision(perpetratorId = p1.id, crimeType = CrimeType.THEFT, foreseenAt = OffsetDateTime.now())
        val v2 = Vision(perpetratorId = p2.id, crimeType = CrimeType.THEFT, foreseenAt = OffsetDateTime.now())
        visionRepository.create(v1)
        visionRepository.create(v2)

        val pa1 = PreArrest(visionId = v1.id, perpetratorId = p1.id, preArrestDate = OffsetDateTime.now())
        val pa2 = PreArrest(visionId = v2.id, perpetratorId = p2.id, preArrestDate = OffsetDateTime.now())
        preArrestRepository.create(pa1)
        preArrestRepository.create(pa2)

        val apology1 = PreApology(
            id = PreApologyId(),
            preArrestId = pa1.id,
            perpetratorId = p1.id,
            compensation = Compensation(10000.0, 450.0, 250.0, 9300.0),
            apologyLetter = ApologyLetter("Sorry 1")
        )
        val apology2 = PreApology(
            id = PreApologyId(),
            preArrestId = pa2.id,
            perpetratorId = p2.id,
            compensation = Compensation(50.0, 450.0, 250.0, -650.0),
            apologyLetter = ApologyLetter("Sorry 2")
        )

        repository.create(apology1)
        repository.create(apology2)

        // WHEN
        val all = repository.findAll()

        // THEN
        assertThat(all).hasSize(2)
        assertThat(all.map { it.perpetratorId }).containsExactlyInAnyOrder(p1.id, p2.id)
    }

    @Test
    fun `should return null when finding non-existing apology`() {
        // WHEN
        val retrieved = repository.findById(PreApologyId(UUID.randomUUID()))

        // THEN
        assertThat(retrieved).isNull()
    }
}
