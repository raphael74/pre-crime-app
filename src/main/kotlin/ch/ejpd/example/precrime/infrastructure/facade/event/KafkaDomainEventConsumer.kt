package ch.ejpd.example.precrime.infrastructure.facade.event

import ch.ejpd.example.precrime.application.AuditApplicationService
import ch.ejpd.example.precrime.application.PreCrimeApplicationService
import ch.ejpd.example.precrime.domain.enforcement.PreArrestExecutedEvent
import ch.ejpd.example.precrime.domain.precog.CrimeForeseenEvent
import ch.ejpd.example.precrime.infrastructure.KafkaTopics
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

private const val PRE_CRIME_GROUP = "pre-crime-group"

@Component
class KafkaDomainEventConsumer(
    private val applicationService: PreCrimeApplicationService,
    private val auditService: AuditApplicationService,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [KafkaTopics.CRIME_FORESEEN_EVENT_TOPIC], groupId = PRE_CRIME_GROUP)
    fun onCrimeForeseen(payload: String) {
        logger.info("📥 Received CrimeForeseenEvent from Kafka")
        val event = objectMapper.readValue(payload, CrimeForeseenEvent::class.java)
        applicationService.onCrimeForeseen(event)
        auditService.logEvent("CrimeForeseenEvent", payload)
    }

    @KafkaListener(topics = [KafkaTopics.PRE_ARREST_EXECUTED_EVENT_TOPIC], groupId = PRE_CRIME_GROUP)
    fun onPreArrestExecuted(payload: String) {
        logger.info("📥 Received PreArrestExecutedEvent from Kafka")
        val event = objectMapper.readValue(payload, PreArrestExecutedEvent::class.java)
        applicationService.onPreArrestExecuted(event)
        auditService.logEvent("PreArrestExecutedEvent", payload)
    }
}