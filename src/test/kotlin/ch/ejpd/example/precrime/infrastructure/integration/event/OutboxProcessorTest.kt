package ch.ejpd.example.precrime.infrastructure.integration.event

import ch.ejpd.example.precrime.domain.perpetrator.PerpetratorId
import ch.ejpd.example.precrime.domain.preapology.PreApologyId
import ch.ejpd.example.precrime.domain.preapology.PreApologyIssuedEvent
import ch.ejpd.example.precrime.domain.prearrest.PreArrestCancelledEvent
import ch.ejpd.example.precrime.domain.prearrest.PreArrestExecutedEvent
import ch.ejpd.example.precrime.domain.prearrest.PreArrestId
import ch.ejpd.example.precrime.domain.vision.CrimeForeseenEvent
import ch.ejpd.example.precrime.domain.vision.CrimeType
import ch.ejpd.example.precrime.domain.vision.VisionId
import ch.ejpd.example.precrime.infrastructure.KafkaTopics.Companion.CRIME_FORESEEN_EVENT_TOPIC
import ch.ejpd.example.precrime.infrastructure.KafkaTopics.Companion.PRE_APOLOGY_ISSUED_EVENT_TOPIC
import ch.ejpd.example.precrime.infrastructure.KafkaTopics.Companion.PRE_ARREST_CANCELLED_EVENT_TOPIC
import ch.ejpd.example.precrime.infrastructure.KafkaTopics.Companion.PRE_ARREST_EXECUTED_EVENT_TOPIC
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.apache.kafka.clients.producer.ProducerRecord
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.transaction.PlatformTransactionManager
import java.time.OffsetDateTime

class OutboxProcessorTest {

    private val transactionManager = mockk<PlatformTransactionManager>(relaxed = true)
    private val outboxRepository = mockk<OutboxRepository>()
    private val kafkaTemplate = mockk<KafkaTemplate<String, Any>>()

    private val processor = OutboxProcessor(
        transactionManager = transactionManager,
        outboxRepository = outboxRepository,
        kafkaTemplate = kafkaTemplate
    )

    @Test
    fun `should do nothing when there are no pending records`() {
        every { outboxRepository.findPendingForUpdate() } returns emptyList()

        processor.processOutbox()

        verify(exactly = 0) { outboxRepository.markAsProcessed(any()) }
        verify(exactly = 0) { outboxRepository.markAsInvalid(any()) }
        verify(exactly = 0) { kafkaTemplate.send(any<ProducerRecord<String, Any>>()) }
    }

    @Test
    fun `should process CrimeForeseenEvent and mark as processed`() {
        val event = CrimeForeseenEvent(VisionId(), PerpetratorId(), CrimeType.MURDER, OffsetDateTime.now())
        val outboxId = OutboxId()
        val outbox = Outbox(outboxId, event, OutboxState.PENDING)
        every { outboxRepository.findPendingForUpdate() } returns listOf(outbox)
        every { outboxRepository.markAsProcessed(outboxId) } returns Unit

        val recordSlot = slot<ProducerRecord<String, Any>>()
        every { kafkaTemplate.send(capture(recordSlot)) } returns mockk {
            every { get() } returns mockk()
        }

        processor.processOutbox()

        verify { outboxRepository.markAsProcessed(outboxId) }
        with(recordSlot.captured) {
            assertThat(topic()).isEqualTo(CRIME_FORESEEN_EVENT_TOPIC)
            assertThat(key()).isEqualTo(event.visionId.value.toString())
            assertThat(value()).isEqualTo(event)
            assertThat(String(headers().lastHeader(IDEMPOTENCE_ID_HEADER).value()))
                .isEqualTo(outboxId.value.toString())
        }
    }

    @Test
    fun `should process PreArrestExecutedEvent and mark as processed`() {
        val event = PreArrestExecutedEvent(PreArrestId(), VisionId(), PerpetratorId())
        val outboxId = OutboxId()
        val outbox = Outbox(outboxId, event, OutboxState.PENDING)
        every { outboxRepository.findPendingForUpdate() } returns listOf(outbox)
        every { outboxRepository.markAsProcessed(outboxId) } returns Unit

        val recordSlot = slot<ProducerRecord<String, Any>>()
        every { kafkaTemplate.send(capture(recordSlot)) } returns mockk {
            every { get() } returns mockk()
        }

        processor.processOutbox()

        verify { outboxRepository.markAsProcessed(outboxId) }
        with(recordSlot.captured) {
            assertThat(topic()).isEqualTo(PRE_ARREST_EXECUTED_EVENT_TOPIC)
            assertThat(key()).isEqualTo(event.preArrestId.value.toString())
            assertThat(value()).isEqualTo(event)
            assertThat(String(headers().lastHeader(IDEMPOTENCE_ID_HEADER).value()))
                .isEqualTo(outboxId.value.toString())
        }
    }

    @Test
    fun `should process PreArrestCancelledEvent and mark as processed`() {
        val event = PreArrestCancelledEvent(PreArrestId(), VisionId(), PerpetratorId())
        val outboxId = OutboxId()
        val outbox = Outbox(outboxId, event, OutboxState.PENDING)
        every { outboxRepository.findPendingForUpdate() } returns listOf(outbox)
        every { outboxRepository.markAsProcessed(outboxId) } returns Unit

        val recordSlot = slot<ProducerRecord<String, Any>>()
        every { kafkaTemplate.send(capture(recordSlot)) } returns mockk {
            every { get() } returns mockk()
        }

        processor.processOutbox()

        verify { outboxRepository.markAsProcessed(outboxId) }
        with(recordSlot.captured) {
            assertThat(topic()).isEqualTo(PRE_ARREST_CANCELLED_EVENT_TOPIC)
            assertThat(key()).isEqualTo(event.preArrestId.value.toString())
            assertThat(value()).isEqualTo(event)
            assertThat(String(headers().lastHeader(IDEMPOTENCE_ID_HEADER).value()))
                .isEqualTo(outboxId.value.toString())
        }
    }

    @Test
    fun `should process PreApologyIssuedEvent and mark as processed`() {
        val event = PreApologyIssuedEvent(PreApologyId(), PreArrestId(), PerpetratorId(), 1000.0)
        val outboxId = OutboxId()
        val outbox = Outbox(outboxId, event, OutboxState.PENDING)
        every { outboxRepository.findPendingForUpdate() } returns listOf(outbox)
        every { outboxRepository.markAsProcessed(outboxId) } returns Unit

        val recordSlot = slot<ProducerRecord<String, Any>>()
        every { kafkaTemplate.send(capture(recordSlot)) } returns mockk {
            every { get() } returns mockk()
        }

        processor.processOutbox()

        verify { outboxRepository.markAsProcessed(outboxId) }
        with(recordSlot.captured) {
            assertThat(topic()).isEqualTo(PRE_APOLOGY_ISSUED_EVENT_TOPIC)
            assertThat(key()).isEqualTo(event.apologyId.value.toString())
            assertThat(value()).isEqualTo(event)
            assertThat(String(headers().lastHeader(IDEMPOTENCE_ID_HEADER).value()))
                .isEqualTo(outboxId.value.toString())
        }
    }

    @Test
    fun `should mark record as invalid when event type is unsupported`() {
        data class UnknownEvent(val id: String)

        val event = UnknownEvent("unknown")
        val outboxId = OutboxId()
        val outbox = Outbox(outboxId, event, OutboxState.PENDING)
        every { outboxRepository.findPendingForUpdate() } returns listOf(outbox)
        every { outboxRepository.markAsInvalid(outboxId) } returns Unit

        processor.processOutbox()

        verify { outboxRepository.markAsInvalid(outboxId) }
        verify(exactly = 0) { outboxRepository.markAsProcessed(any()) }
        verify(exactly = 0) { kafkaTemplate.send(any<ProducerRecord<String, Any>>()) }
    }

    @Test
    fun `should not mark record as processed or invalid when kafka send fails`() {
        val event = CrimeForeseenEvent(VisionId(), PerpetratorId(), CrimeType.MURDER, OffsetDateTime.now())
        val outboxId = OutboxId()
        val outbox = Outbox(outboxId, event, OutboxState.PENDING)
        every { outboxRepository.findPendingForUpdate() } returns listOf(outbox)
        every { kafkaTemplate.send(any<ProducerRecord<String, Any>>()) } returns mockk {
            every { get() } throws RuntimeException("Kafka unavailable")
        }

        processor.processOutbox()

        verify(exactly = 0) { outboxRepository.markAsProcessed(any()) }
        verify(exactly = 0) { outboxRepository.markAsInvalid(any()) }
    }

    @Test
    fun `should process multiple records in one cycle`() {
        val visionId = VisionId()
        val event1 = CrimeForeseenEvent(visionId, PerpetratorId(), CrimeType.MURDER, OffsetDateTime.now())
        val event2 = PreArrestExecutedEvent(PreArrestId(), VisionId(), PerpetratorId())
        val outboxId1 = OutboxId()
        val outboxId2 = OutboxId()
        val outbox1 = Outbox(outboxId1, event1, OutboxState.PENDING)
        val outbox2 = Outbox(outboxId2, event2, OutboxState.PENDING)
        every { outboxRepository.findPendingForUpdate() } returns listOf(outbox1, outbox2)
        every { outboxRepository.markAsProcessed(outboxId1) } returns Unit
        every { outboxRepository.markAsProcessed(outboxId2) } returns Unit
        every { kafkaTemplate.send(any<ProducerRecord<String, Any>>()) } returns mockk {
            every { get() } returns mockk()
        }

        processor.processOutbox()

        verify { outboxRepository.markAsProcessed(outboxId1) }
        verify { outboxRepository.markAsProcessed(outboxId2) }
    }
}
