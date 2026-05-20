package ch.ejpd.example.precrime.infrastructure.integration.persistence

import ch.ejpd.example.precrime.IntegrationTest
import ch.ejpd.example.precrime.domain.apology.ApologyLetter
import ch.ejpd.example.precrime.domain.apology.Compensation
import ch.ejpd.example.precrime.domain.apology.PreApology
import ch.ejpd.example.precrime.domain.apology.PreApologyId
import ch.ejpd.example.precrime.domain.precog.Perpetrator
import ch.ejpd.example.precrime.domain.precog.VisionId
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

    @Test
    fun `should persist and retrieve pre-emptive apology`() {
        // GIVEN
        val apologyId = PreApologyId()
        val visionId = VisionId()
        val perpetrator = Perpetrator("Danny Witwer")
        val compensation = Compensation(10000.0, 450.0, 250.0, 9300.0)
        val apologyLetter = ApologyLetter("Dear Family, we are sorry.")
        val apology = PreApology(apologyId, visionId, perpetrator, compensation, apologyLetter)

        // WHEN
        repository.save(apology)

        // THEN
        val retrieved = repository.findById(apologyId)
        assertThat(retrieved).isNotNull
        assertThat(retrieved!!.id).isEqualTo(apologyId)
        assertThat(retrieved.visionId).isEqualTo(visionId)
        assertThat(retrieved.perpetrator.name).isEqualTo("Danny Witwer")
        assertThat(retrieved.compensation.baseAmount).isEqualTo(10000.0)
        assertThat(retrieved.compensation.netPayout).isEqualTo(9300.0)
        assertThat(retrieved.apologyLetter.text).isEqualTo("Dear Family, we are sorry.")
    }

    @Test
    fun `should retrieve all persisted apologies`() {
        // GIVEN
        val apology1 = PreApology(
            id = PreApologyId(),
            visionId = VisionId(),
            perpetrator = Perpetrator("Danny Witwer"),
            compensation = Compensation(10000.0, 450.0, 250.0, 9300.0),
            apologyLetter = ApologyLetter("Sorry 1")
        )
        val apology2 = PreApology(
            id = PreApologyId(),
            visionId = VisionId(),
            perpetrator = Perpetrator("Arthur Pendelton"),
            compensation = Compensation(50.0, 450.0, 250.0, -650.0),
            apologyLetter = ApologyLetter("Sorry 2")
        )

        repository.save(apology1)
        repository.save(apology2)

        // WHEN
        val all = repository.findAll()

        // THEN
        assertThat(all).hasSize(2)
        assertThat(all.map { it.perpetrator.name }).containsExactlyInAnyOrder("Danny Witwer", "Arthur Pendelton")
    }

    @Test
    fun `should return null when finding non-existing apology`() {
        // WHEN
        val retrieved = repository.findById(PreApologyId(UUID.randomUUID()))

        // THEN
        assertThat(retrieved).isNull()
    }
}
