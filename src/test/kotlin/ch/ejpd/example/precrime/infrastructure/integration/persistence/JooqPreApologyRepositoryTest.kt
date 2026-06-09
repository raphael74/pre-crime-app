package ch.ejpd.example.precrime.infrastructure.integration.persistence

import ch.ejpd.example.precrime.IntegrationTest
import ch.ejpd.example.precrime.domain.perpetrator.Perpetrator
import ch.ejpd.example.precrime.domain.perpetrator.PerpetratorRepository
import ch.ejpd.example.precrime.domain.preapology.ApologyLetter
import ch.ejpd.example.precrime.domain.preapology.Compensation
import ch.ejpd.example.precrime.domain.preapology.PreApology
import ch.ejpd.example.precrime.domain.preapology.PreApologyId
import ch.ejpd.example.precrime.domain.prearrest.PreArrestId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.util.*

@IntegrationTest
@Transactional
class JooqPreApologyRepositoryTest {

    @Autowired
    private lateinit var repository: JooqPreApologyRepository

    @Autowired
    private lateinit var perpetratorRepository: PerpetratorRepository

    @Test
    fun `should persist and retrieve pre-emptive apology`() {
        // GIVEN
        val apologyId = PreApologyId()
        val preArrestId = PreArrestId()
        val perpetrator = Perpetrator(firstName = "Danny", lastName = "Witwer")
        perpetratorRepository.create(perpetrator)
        val compensation = Compensation(10000.0, 450.0, 250.0, 9300.0)
        val apologyLetter = ApologyLetter("Dear Family, we are sorry.")
        val apology =
            PreApology(apologyId, preArrestId, perpetrator.id, compensation, apologyLetter)

        // WHEN
        repository.save(apology)

        // THEN
        val retrieved = repository.findById(apologyId)
        assertThat(retrieved).isNotNull
        assertThat(retrieved!!.id).isEqualTo(apologyId)
        assertThat(retrieved.preArrestId).isEqualTo(preArrestId)
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

        val now = java.time.OffsetDateTime.now()
        val apology1 = PreApology(
            id = PreApologyId(),
            preArrestId = PreArrestId(),
            perpetratorId = p1.id,
            compensation = Compensation(100.0, 0.0, 0.0, 100.0),
            apologyLetter = ApologyLetter("Sorry 1"),
            createdAt = now.minusDays(2)
        )
        val apology2 = PreApology(
            id = PreApologyId(),
            preArrestId = PreArrestId(),
            perpetratorId = p2.id,
            compensation = Compensation(100.0, 0.0, 0.0, 100.0),
            apologyLetter = ApologyLetter("Sorry 2"),
            createdAt = now
        )
        val apology3 = PreApology(
            id = PreApologyId(),
            preArrestId = PreArrestId(),
            perpetratorId = p3.id,
            compensation = Compensation(100.0, 0.0, 0.0, 100.0),
            apologyLetter = ApologyLetter("Sorry 3"),
            createdAt = now.minusDays(1)
        )

        repository.save(apology1)
        repository.save(apology2)
        repository.save(apology3)

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

        val apology1 = PreApology(
            id = PreApologyId(),
            preArrestId = PreArrestId(),
            perpetratorId = p1.id,
            compensation = Compensation(10000.0, 450.0, 250.0, 9300.0),
            apologyLetter = ApologyLetter("Sorry 1")
        )
        val apology2 = PreApology(
            id = PreApologyId(),
            preArrestId = PreArrestId(),
            perpetratorId = p2.id,
            compensation = Compensation(50.0, 450.0, 250.0, -650.0),
            apologyLetter = ApologyLetter("Sorry 2")
        )

        repository.save(apology1)
        repository.save(apology2)

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
