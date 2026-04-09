package ch.ejpd.example.precrime.infrastructure.integration.persistence

import ch.ejpd.example.precrime.IntegrationTest
import ch.ejpd.example.precrime.domain.audit.AuditEntry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

@IntegrationTest
@Transactional
class JooqAuditEntryRepositoryTest {

    @Autowired
    private lateinit var repository: JooqAuditEntryRepository

    @Test
    fun `should save and find all audit entries`() {
        // GIVEN
        val entry1 = AuditEntry(
            eventType = "CrimeForeseen",
            payload = "{\"perpetrator\":\"Alice\"}",
            recordedAt = OffsetDateTime.now().minusMinutes(1).truncatedTo(ChronoUnit.MILLIS)
        )
        val entry2 = AuditEntry(
            eventType = "PreArrestExecuted",
            payload = "{\"perpetrator\":\"Alice\"}",
            recordedAt = OffsetDateTime.now().truncatedTo(ChronoUnit.MILLIS)
        )

        // WHEN
        repository.save(entry1)
        repository.save(entry2)

        // THEN
        val allEntries = repository.findAll()
        assertThat(allEntries).hasSize(2)

        // Ordered by recordedAt desc
        assertThat(allEntries[0].id).isEqualTo(entry2.id)
        assertThat(allEntries[0].eventType).isEqualTo(entry2.eventType)
        assertThat(allEntries[0].payload).isEqualTo(entry2.payload)
        assertThat(allEntries[0].recordedAt.truncatedTo(ChronoUnit.MILLIS))
            .isEqualTo(entry2.recordedAt.truncatedTo(ChronoUnit.MILLIS))

        assertThat(allEntries[1].id).isEqualTo(entry1.id)
        assertThat(allEntries[1].eventType).isEqualTo(entry1.eventType)
        assertThat(allEntries[1].payload).isEqualTo(entry1.payload)
        assertThat(allEntries[1].recordedAt.truncatedTo(ChronoUnit.MILLIS))
            .isEqualTo(entry1.recordedAt.truncatedTo(ChronoUnit.MILLIS))
    }

    @Test
    fun `should return empty list when no audit entries exist`() {
        // WHEN
        val allEntries = repository.findAll()

        // THEN
        assertThat(allEntries).isEmpty()
    }
}
