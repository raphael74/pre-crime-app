package ch.ejpd.example.precrime.infrastructure.integration.persistence

import ch.ejpd.example.precrime.IntegrationTest
import ch.ejpd.example.precrime.domain.enforcement.PreArrestExecutedEvent
import ch.ejpd.example.precrime.domain.enforcement.PreArrestId
import ch.ejpd.example.precrime.domain.precog.CrimeForeseenEvent
import ch.ejpd.example.precrime.domain.precog.CrimeType
import ch.ejpd.example.precrime.domain.precog.Perpetrator
import ch.ejpd.example.precrime.domain.precog.VisionId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@IntegrationTest
@Transactional
class JooqOutboxRepositoryTest {

    @Autowired
    private lateinit var outboxRepository: JooqOutboxRepository

    @Test
    fun `should create and read an outbox entry`() {
        // GIVEN
        val testEvent = CrimeForeseenEvent(VisionId(), Perpetrator("joe"), CrimeType("murder"), LocalDateTime.now())

        // WHEN
        val id = outboxRepository.create(testEvent)

        // THEN
        val result = outboxRepository.findById(id)
        assertThat(result).isNotNull
        assertThat(result?.id).isEqualTo(id)
        assertThat(result?.event).isEqualTo(testEvent)
    }

    @Test
    fun `should find pending records for update`() {
        // GIVEN
        val id1 = outboxRepository.create(
            CrimeForeseenEvent(
                VisionId(),
                Perpetrator("joe"),
                CrimeType("murder"),
                LocalDateTime.now()
            )
        )
        val id2 = outboxRepository.create(PreArrestExecutedEvent(PreArrestId(), VisionId(), Perpetrator("jane")))

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
        val id = outboxRepository.create(PreArrestExecutedEvent(PreArrestId(), VisionId(), Perpetrator("jane")))

        // WHEN
        outboxRepository.markAsProcessed(id)

        // THEN
        val pending = outboxRepository.findPendingForUpdate()
        assertThat(pending.map { it.id }).doesNotContain(id)
    }
}
