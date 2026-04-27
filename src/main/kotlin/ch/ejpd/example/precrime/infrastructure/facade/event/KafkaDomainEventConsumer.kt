package ch.ejpd.example.precrime.infrastructure.facade.event

import ch.ejpd.example.precrime.application.AuditApplicationService
import ch.ejpd.example.precrime.application.PreCrimeApplicationService
import ch.ejpd.example.precrime.domain.enforcement.PreArrestExecutedEvent
import ch.ejpd.example.precrime.domain.precog.CrimeForeseenEvent
import ch.ejpd.example.precrime.infrastructure.KafkaTopics
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

private const val PRE_CRIME_GROUP = "pre-crime-group"
private const val PRE_CRIME_AUDIT_GROUP = "pre-crime-audit-group"

@Component
class KafkaDomainEventConsumer(
    private val applicationService: PreCrimeApplicationService,
    private val auditService: AuditApplicationService,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [KafkaTopics.CRIME_FORESEEN_EVENT_TOPIC], groupId = PRE_CRIME_GROUP)
    fun onCrimeForeseen(event: CrimeForeseenEvent) {
        logger.info("📥 Received CrimeForeseenEvent from Kafka")
        applicationService.onCrimeForeseen(event)
    }

    @KafkaListener(topics = [KafkaTopics.PRE_ARREST_EXECUTED_EVENT_TOPIC], groupId = PRE_CRIME_GROUP)
    fun onPreArrestExecuted(event: PreArrestExecutedEvent) {
        logger.info("📥 Received PreArrestExecutedEvent from Kafka")
        applicationService.onPreArrestExecuted(event)
    }

    @KafkaListener(
        topics = [KafkaTopics.CRIME_FORESEEN_EVENT_TOPIC, KafkaTopics.PRE_ARREST_EXECUTED_EVENT_TOPIC],
        groupId = PRE_CRIME_AUDIT_GROUP
    )
    fun onPreCrimeEvent(consumerRecord: ConsumerRecord<String, Any>) {
        auditService.logEvent(
            consumerRecord.value()::class.simpleName ?: "None",
            objectMapper.writeValueAsString(consumerRecord.value())
        )
    }
}