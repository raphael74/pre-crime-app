package ch.ejpd.example.precrime.infrastructure.integration.event

import ch.ejpd.example.precrime.infrastructure.integration.persistence.JooqOutboxRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class TransactionalOutboxProcessor(
    private val outboxRepository: JooqOutboxRepository,
    private val kafkaTemplate: KafkaTemplate<String, String>
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 1000)
    @Transactional
    fun processOutbox() {
        val outboxRecords = outboxRepository.findPendingForUpdate()
        logger.info("Found ${outboxRecords.size} events to process")

        outboxRecords.forEach { outboxRecord ->
            sendKafkaEvent(outboxRecord.topic, outboxRecord.eventKey, outboxRecord.eventType, outboxRecord.payload)
            outboxRepository.markAsProcessed(outboxRecord.id)
        }
    }

    private fun sendKafkaEvent(topic: String, eventKey: String, eventType: String, payload: String) {
        logger.info("Sending Kafka event $eventType to topic $topic with key $eventKey")
        kafkaTemplate.send(topic, eventKey, payload).get()
    }
}
