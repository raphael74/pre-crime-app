package ch.ejpd.example.precrime.infrastructure.integration.persistence

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestPropertySource
import org.springframework.transaction.annotation.Transactional
import java.util.*

@SpringBootTest
@EmbeddedKafka(partitions = 1)
@TestPropertySource("/application-test.properties")
@Transactional
@DirtiesContext
class JooqOutboxRepositoryTest {

    @Autowired
    private lateinit var outboxRepository: JooqOutboxRepository

    @Test
    fun `should create and read an outbox entry`() {
        // GIVEN
        val eventType = "PRECIME_TEST_EVENT"
        val payload = """{"caseId":"${UUID.randomUUID()}"}"""

        // WHEN
        val id = outboxRepository.create(eventType, payload)

        // THEN
        val result = outboxRepository.findById(id)
        assertThat(result).isNotNull
        assertThat(result?.id).isEqualTo(id)
        assertThat(result?.eventType).isEqualTo(eventType)
        assertThat(result?.payload).isEqualTo(payload)
    }

    @Test
    fun `should find pending records for update`() {
        // GIVEN
        val id1 = outboxRepository.create("EVENT_1", "{}")
        val id2 = outboxRepository.create("EVENT_2", "{}")

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
        val id = outboxRepository.create("PROCESSED_TEST", "{}")

        // WHEN
        outboxRepository.markAsProcessed(id)

        // THEN
        val pending = outboxRepository.findPendingForUpdate()
        assertThat(pending.map { it.id }).doesNotContain(id)
    }
}
