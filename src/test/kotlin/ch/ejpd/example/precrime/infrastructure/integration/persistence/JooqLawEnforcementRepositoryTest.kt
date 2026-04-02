package ch.ejpd.example.precrime.infrastructure.integration.persistence

import ch.ejpd.example.precrime.IntegrationTest
import ch.ejpd.example.precrime.domain.DomainEventPublisher
import ch.ejpd.example.precrime.domain.enforcement.EnforcementUnitId
import ch.ejpd.example.precrime.domain.enforcement.LawEnforcementUnit
import ch.ejpd.example.precrime.domain.precog.VisionId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.util.*

@IntegrationTest
class JooqLawEnforcementRepositoryTest {

    @Autowired
    private lateinit var repository: JooqLawEnforcementRepository

    @MockitoBean
    private lateinit var domainEventPublisher: DomainEventPublisher

    private val SINGLETON_ID = EnforcementUnitId(UUID.fromString("00000000-0000-0000-0000-000000000002"))

    @Test
    fun `should find singleton unit and it should be able to publish events`() {
        // WHEN
        val unit = repository.findSingleton()

        // THEN
        assertThat(unit).isNotNull
        assertThat(unit.id).isEqualTo(SINGLETON_ID)
        assertThat(unit.unitName).isEqualTo("Pre-Crime Team Alpha")

        // AND WHEN the aggregate publishes an event
        val visionId = VisionId()
        val perpetrator = "John Doe"
        val event = unit.executePreArrest(visionId, perpetrator)

        // THEN the injected publisher should have been called
        verify(domainEventPublisher).publish(event)
    }

    @Test
    fun `should save unit name change`() {
        // GIVEN
        val unit = repository.findSingleton()
        val newName = "New Unit Name"
        val updatedUnit = LawEnforcementUnit(unit.id, newName)

        // WHEN
        repository.save(updatedUnit)

        // THEN
        val result = repository.findSingleton()
        assertThat(result.unitName).isEqualTo(newName)
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
