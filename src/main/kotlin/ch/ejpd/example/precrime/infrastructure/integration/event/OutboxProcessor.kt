package ch.ejpd.example.precrime.infrastructure.integration.event

import ch.ejpd.example.precrime.domain.preapology.PreApologyIssuedEvent
import ch.ejpd.example.precrime.domain.prearrest.PreArrestCancelledEvent
import ch.ejpd.example.precrime.domain.prearrest.PreArrestExecutedEvent
import ch.ejpd.example.precrime.domain.vision.CrimeForeseenEvent
import ch.ejpd.example.precrime.infrastructure.KafkaTopics.Companion.CRIME_FORESEEN_EVENT_TOPIC
import ch.ejpd.example.precrime.infrastructure.KafkaTopics.Companion.PRE_APOLOGY_ISSUED_EVENT_TOPIC
import ch.ejpd.example.precrime.infrastructure.KafkaTopics.Companion.PRE_ARREST_CANCELLED_EVENT_TOPIC
import ch.ejpd.example.precrime.infrastructure.KafkaTopics.Companion.PRE_ARREST_EXECUTED_EVENT_TOPIC
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

const val IDEMPOTENCE_ID_HEADER = "x-idempotence-id"

@Component
class OutboxProcessor(
    transactionManager: PlatformTransactionManager,
    private val outboxRepository: OutboxRepository,
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val transactionTemplate = TransactionTemplate(transactionManager)

    @Scheduled(fixedDelay = 1000)
    fun processOutbox() {
        val outboxRecords = outboxRepository.findPendingForUpdate()
        if (outboxRecords.isNotEmpty()) {
            logger.info("Found ${outboxRecords.size} events to process")
        }
        outboxRecords.forEach { sendEvent(it.id, it.event) }
    }

    private fun sendEvent(outboxId: OutboxId, event: Any) {
        transactionTemplate.executeWithoutResult {
            var record: ProducerRecord<String, Any>? = null
            try {
                record = prepareKafkaEvent(outboxId, event)
            } catch (e: Exception) {
                logger.error("Error while preparing event with id ${outboxId.value}", e)
                outboxRepository.markAsInvalid(outboxId)
            }
            if (record != null) {
                try {
                    logger.info("Sending Kafka event ${event::class.simpleName} with id ${outboxId.value} to topic ${record.topic()} with key ${record.key()}")
                    kafkaTemplate.send(record).get()
                    outboxRepository.markAsProcessed(outboxId)
                } catch (e: Exception) {
                    logger.error("Error while sending event with id ${outboxId.value}", e)
                }
            }
        }
    }

    private fun prepareKafkaEvent(outboxId: OutboxId, event: Any): ProducerRecord<String, Any> {
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

            is PreArrestCancelledEvent -> {
                topic = PRE_ARREST_CANCELLED_EVENT_TOPIC
                eventKey = event.preArrestId.value.toString()
            }

            is PreApologyIssuedEvent -> {
                topic = PRE_APOLOGY_ISSUED_EVENT_TOPIC
                eventKey = event.apologyId.value.toString()
            }

            else -> throw IllegalArgumentException("Unsupported event type: ${event::class.simpleName}")
        }

        val record = ProducerRecord(topic, eventKey, event)
        record.headers().add(IDEMPOTENCE_ID_HEADER, outboxId.value.toString().toByteArray())
        return record
    }
}