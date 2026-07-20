package org.example.precrime.infrastructure.integration.event

import org.example.precrime.domain.perpetrator.PerpetratorId
import org.example.precrime.domain.prearrest.PreArrestExecutedEvent
import org.example.precrime.domain.prearrest.PreArrestId
import org.example.precrime.domain.vision.CrimeForeseenEvent
import org.example.precrime.domain.vision.CrimeType
import org.example.precrime.domain.vision.VisionId
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

class TransactionalDomainEventPublisherTest {

    private val outboxRepository = mockk<OutboxRepository>()
    private val publisher = TransactionalDomainEventPublisher(outboxRepository)

    @Test
    fun `publish should create outbox entry for single event`() {
        val event = CrimeForeseenEvent(VisionId(), PerpetratorId(), CrimeType.MURDER, OffsetDateTime.now())
        every { outboxRepository.create(any()) } returns OutboxId()

        publisher.publish(event)

        verify(exactly = 1) { outboxRepository.create(event) }
    }

    @Test
    fun `publish should create outbox entries for each event in iterable`() {
        val event1 = CrimeForeseenEvent(VisionId(), PerpetratorId(), CrimeType.MURDER, OffsetDateTime.now())
        val event2 = PreArrestExecutedEvent(PreArrestId(), VisionId(), PerpetratorId())
        every { outboxRepository.create(any()) } returns OutboxId()

        publisher.publish(listOf(event1, event2))

        verify(exactly = 1) { outboxRepository.create(event1) }
        verify(exactly = 1) { outboxRepository.create(event2) }
    }

    @Test
    fun `publish should skip null elements in iterable`() {
        val event = CrimeForeseenEvent(VisionId(), PerpetratorId(), CrimeType.MURDER, OffsetDateTime.now())
        every { outboxRepository.create(any()) } returns OutboxId()

        publisher.publish(listOf(event, null))

        verify(exactly = 1) { outboxRepository.create(any()) }
    }

    @Test
    fun `publish should handle empty iterable`() {
        publisher.publish(emptyList<Any>())

        verify(exactly = 0) { outboxRepository.create(any()) }
    }
}
