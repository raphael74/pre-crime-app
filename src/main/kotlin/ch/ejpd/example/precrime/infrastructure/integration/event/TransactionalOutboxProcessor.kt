package ch.ejpd.example.precrime.infrastructure.integration.event

import ch.ejpd.example.precrime.domain.enforcement.PreArrestExecutedEvent
import ch.ejpd.example.precrime.domain.precog.CrimeForeseenEvent
import ch.ejpd.example.precrime.infrastructure.KafkaTopics.Companion.CRIME_FORESEEN_EVENT_TOPIC
import ch.ejpd.example.precrime.infrastructure.KafkaTopics.Companion.PRE_ARREST_EXECUTED_EVENT_TOPIC
import ch.ejpd.example.precrime.infrastructure.integration.persistence.JooqOutboxRepository
import ch.ejpd.example.precrime.infrastructure.integration.persistence.OutboxId
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class TransactionalOutboxProcessor(
    private val outboxRepository: JooqOutboxRepository,
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 1000)
    @Transactional
    fun processOutbox() {
        val outboxRecords = outboxRepository.findPendingForUpdate()
        if (outboxRecords.isNotEmpty()) {
            logger.info("Found ${outboxRecords.size} events to process")
        }

        outboxRecords.forEach { outboxRecord ->
            sendKafkaEvent(outboxRecord.id, outboxRecord.event)
            outboxRepository.markAsProcessed(outboxRecord.id)
        }
    }

    private fun sendKafkaEvent(outboxId: OutboxId, event: Any) {
        val topic: String
        val eventKey: String

        when (event) {
            is CrimeForeseenEvent -> {
                topic = CRIME_FORESEEN_EVENT_TOPIC
                eventKey = event.visionId.value.toString()
            }

            is PreArrestExecutedEvent -> {
                topic = PRE_ARREST_EXECUTED_EVENT_TOPIC
                eventKey = event.preArrestId.value.toString()
            }

            else -> throw IllegalArgumentException("Unsupported event type: ${event::class.simpleName}")
        }

        logger.info("Sending Kafka event ${event::class.simpleName} with ID ${outboxId.value} to topic $topic with key $eventKey")

        val record = org.apache.kafka.clients.producer.ProducerRecord(topic, eventKey, event)
        record.headers().add("X-Event-Id", outboxId.value.toString().toByteArray())

        kafkaTemplate.send(record).get()
    }
}
