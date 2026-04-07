package ch.ejpd.example.precrime.infrastructure.integration.persistence

import ch.ejpd.example.precrime.IntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

@IntegrationTest
class JooqOutboxRepositoryTest {

    @Autowired
    private lateinit var outboxRepository: JooqOutboxRepository

    @Test
    fun `should create and read an outbox entry`() {
        // GIVEN
        val eventType = "PRECIME_TEST_EVENT"
        val topic = "test-topic"
        val key = "test-key"
        val payload = """{"caseId":"${UUID.randomUUID()}"}"""

        // WHEN
        val id = outboxRepository.create(eventType, topic, key, payload)

        // THEN
        val result = outboxRepository.findById(id)
        assertThat(result).isNotNull
        assertThat(result?.id).isEqualTo(id)
        assertThat(result?.eventType).isEqualTo(eventType)
        assertThat(result?.topic).isEqualTo(topic)
        assertThat(result?.eventKey).isEqualTo(key)
        assertThat(result?.payload).isEqualTo(payload)
    }

    @Test
    fun `should find pending records for update`() {
        // GIVEN
        val id1 = outboxRepository.create("EVENT_1", "topic-1", "key-1", "{}")
        val id2 = outboxRepository.create("EVENT_2", "topic-2", "key-2", "{}")

        // WHEN
        val pending = outboxRepository.findPendingForUpdate()

        // THEN
        assertThat(pending).hasSizeGreaterThanOrEqualTo(2)
        val pendingIds = pending.map { it.id }
        assertThat(pendingIds).contains(id1, id2)
    }

    @Test
    fun `should mark record as processed`() {
        // GIVEN
        val id = outboxRepository.create("PROCESSED_TEST", "topic-3", "key-3", "{}")

        // WHEN
        outboxRepository.markAsProcessed(id)

        // THEN
        val pending = outboxRepository.findPendingForUpdate()
        assertThat(pending.map { it.id }).doesNotContain(id)
    }
}
