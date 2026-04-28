package ch.ejpd.example.precrime.infrastructure.facade.event

import ch.ejpd.example.precrime.application.AuditApplicationService
import ch.ejpd.example.precrime.application.PreCrimeApplicationService
import ch.ejpd.example.precrime.domain.enforcement.PreArrestExecutedEvent
import ch.ejpd.example.precrime.domain.precog.CrimeForeseenEvent
import ch.ejpd.example.precrime.infrastructure.KafkaTopics
import ch.ejpd.example.precrime.infrastructure.integration.event.IDEMPOTENCE_ID_HEADER
import ch.ejpd.example.precrime.infrastructure.integration.persistence.JooqInboxRepository
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper
import java.util.*

private const val PRE_CRIME_GROUP = "pre-crime-group"
private const val PRE_CRIME_AUDIT_GROUP = "pre-crime-audit-group"

@Component
class KafkaDomainEventConsumer(
    private val applicationService: PreCrimeApplicationService,
    private val auditService: AuditApplicationService,
    private val inboxRepository: JooqInboxRepository,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    @KafkaListener(topics = [KafkaTopics.CRIME_FORESEEN_EVENT_TOPIC], groupId = PRE_CRIME_GROUP)
    fun onCrimeForeseen(
        event: CrimeForeseenEvent,
        @Header(IDEMPOTENCE_ID_HEADER) idempotenceId: String
    ) {
        checkIdempotence(idempotenceId, PRE_CRIME_GROUP) {
            applicationService.onCrimeForeseen(event)
        }
    }

    @Transactional
    @KafkaListener(topics = [KafkaTopics.PRE_ARREST_EXECUTED_EVENT_TOPIC], groupId = PRE_CRIME_GROUP)
    fun onPreArrestExecuted(
        event: PreArrestExecutedEvent,
        @Header(IDEMPOTENCE_ID_HEADER) idempotenceId: String
    ) {
        checkIdempotence(idempotenceId, PRE_CRIME_GROUP) {
            applicationService.onPreArrestExecuted(event)
        }
    }

    @Transactional
    @KafkaListener(
        topics = [KafkaTopics.CRIME_FORESEEN_EVENT_TOPIC, KafkaTopics.PRE_ARREST_EXECUTED_EVENT_TOPIC],
        groupId = PRE_CRIME_AUDIT_GROUP
    )
    fun onPreCrimeEvent(
        consumerRecord: ConsumerRecord<String, Any>,
        @Header(IDEMPOTENCE_ID_HEADER) idempotenceId: String
    ) {
        checkIdempotence(idempotenceId, PRE_CRIME_AUDIT_GROUP) {
            auditService.logEvent(
                consumerRecord.value()::class.simpleName ?: "None",
                objectMapper.writeValueAsString(consumerRecord.value())
            )
        }
    }

    private fun checkIdempotence(
        idempotenceId: String,
        consumerGroup: String,
        action: () -> Unit
    ) {
        if (inboxRepository.insertIfNotExists(UUID.fromString(idempotenceId), consumerGroup)) {
            action()
        } else {
            logger.info("Skipping duplicate event with idempotenceId $idempotenceId")
        }
    }
}