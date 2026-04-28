package ch.ejpd.example.precrime.infrastructure.integration.persistence

import ch.ejpd.example.precrime.IntegrationTest
import ch.ejpd.example.precrime.domain.enforcement.EnforcementUnitId
import ch.ejpd.example.precrime.domain.enforcement.LawEnforcementUnit
import ch.ejpd.example.precrime.domain.enforcement.PreArrestStatus
import ch.ejpd.example.precrime.domain.enforcement.UnitName
import ch.ejpd.example.precrime.domain.precog.Perpetrator
import ch.ejpd.example.precrime.domain.precog.VisionId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.util.*

@IntegrationTest
@Transactional
class JooqLawEnforcementRepositoryTest {

    @Autowired
    private lateinit var repository: JooqLawEnforcementRepository

    private val SINGLETON_ID = EnforcementUnitId(UUID.fromString("00000000-0000-0000-0000-000000000002"))

    @Test
    fun `should find singleton unit`() {
        // WHEN
        val unit = repository.findSingleton()

        // THEN
        assertThat(unit).isNotNull
        assertThat(unit.id).isEqualTo(SINGLETON_ID)
    }

    @Test
    fun `should save unit name change`() {
        // GIVEN
        val unit = repository.findSingleton()
        val newName = UnitName("New Unit Name")
        val updatedUnit = LawEnforcementUnit(id = unit.id, unitName = newName)

        // WHEN
        repository.save(updatedUnit)

        // THEN
        val result = repository.findSingleton()
        assertThat(result.unitName).isEqualTo(newName)
    }

    @Test
    fun `should persist pre-arrests in collection`() {
        // GIVEN
        val unit = repository.findSingleton()
        val visionId = VisionId()
        val perpetrator = Perpetrator("Perpetrator X")

        // WHEN
        unit.executePreArrest(visionId, perpetrator)
        repository.save(unit)

        // THEN
        val result = repository.findSingleton()
        assertThat(result.preArrests).hasSize(1)
        assertThat(result.preArrests.first().perpetrator).isEqualTo(perpetrator)
        assertThat(result.preArrests.first().status).isEqualTo(PreArrestStatus.ARRESTED_BEFORE_CRIME)
    }

    @Test
    fun `should return null when finding by non-existing id`() {
        // GIVEN
        val randomId = EnforcementUnitId(UUID.randomUUID())

        // WHEN
        val result = repository.findById(randomId)

        // THEN
        assertThat(result).isNull()
    }
}
