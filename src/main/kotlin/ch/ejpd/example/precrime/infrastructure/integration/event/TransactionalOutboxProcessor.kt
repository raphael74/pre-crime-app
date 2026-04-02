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
            sendKafkaEvent(outboxRecord.eventType, outboxRecord.payload)
            outboxRepository.markAsProcessed(outboxRecord.id)
        }
    }

    private fun sendKafkaEvent(eventType: String, payload: String) {
        val topic = eventType.substringAfterLast(".")
            .replace(Regex("([a-z])([A-Z])"), "$1-$2")
            .lowercase()
        kafkaTemplate.send(topic, eventType, payload).get()
    }
}
