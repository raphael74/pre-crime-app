package ch.ejpd.example.precrime.infrastructure.integration.persistence

import ch.ejpd.example.precrime.IntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.util.*

@IntegrationTest
@Transactional
class JooqInboxRepositoryTest(
    @Autowired private var repository: JooqInboxRepository
) {

    @Test
    fun `insertIfNotExists should return true for new id`() {
        // GIVEN
        val id = UUID.randomUUID()
        val consumerGroup = "test-group"

        // WHEN
        val result = repository.insertIfNotExists(id, consumerGroup)

        // THEN
        assertThat(result).isTrue()
    }

    @Test
    fun `insertIfNotExists should return false for duplicate id`() {
        // GIVEN
        val id = UUID.randomUUID()
        val consumerGroup = "test-group"
        repository.insertIfNotExists(id, consumerGroup)

        // WHEN
        val result = repository.insertIfNotExists(id, consumerGroup)

        // THEN
        assertThat(result).isFalse()
    }

    @Test
    fun `insertIfNotExists should allow same id in different consumer groups`() {
        // GIVEN
        val id = UUID.randomUUID()
        repository.insertIfNotExists(id, "group-a")

        // WHEN
        val result = repository.insertIfNotExists(id, "group-b")

        // THEN
        assertThat(result).isTrue()
    }
}
