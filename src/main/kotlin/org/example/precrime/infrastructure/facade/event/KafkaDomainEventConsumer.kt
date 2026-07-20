package org.example.precrime.infrastructure.facade.event

import org.example.precrime.application.AuditApplicationService
import org.example.precrime.application.PreCrimeApplicationService
import org.example.precrime.domain.prearrest.PreArrestExecutedEvent
import org.example.precrime.domain.vision.CrimeForeseenEvent
import org.example.precrime.infrastructure.KafkaTopics
import org.example.precrime.infrastructure.integration.event.IDEMPOTENCE_ID_HEADER
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.BackOff
import org.springframework.kafka.annotation.DltHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.RetryableTopic
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy
import org.springframework.kafka.support.KafkaHeaders
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
    private val inboxRepository: InboxRepository,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    @RetryableTopic(
        attempts = "4",
        backOff = BackOff(delay = 1000, multiplier = 2.0),
        topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
    )
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
    @RetryableTopic(
        attempts = "4",
        backOff = BackOff(delay = 1000, multiplier = 2.0),
        topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
    )
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
    @RetryableTopic(
        attempts = "4",
        backOff = BackOff(delay = 1000, multiplier = 2.0),
        topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
    )
    @KafkaListener(
        topics = [KafkaTopics.CRIME_FORESEEN_EVENT_TOPIC, KafkaTopics.PRE_ARREST_EXECUTED_EVENT_TOPIC, KafkaTopics.PRE_APOLOGY_ISSUED_EVENT_TOPIC, KafkaTopics.PRE_ARREST_CANCELLED_EVENT_TOPIC],
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

    @DltHandler
    fun handleDlt(
        payload: Any,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.GROUP_ID) groupId: String,
        @Header(IDEMPOTENCE_ID_HEADER) idempotenceId: String
    ) {
        logger.error("Event moved to DLT. Topic: $topic, Group: $groupId, ID: $idempotenceId, Payload: $payload")
    }

    private fun checkIdempotence(
        idempotenceId: String,
        consumerGroup: String,
        action: () -> Unit
    ) {
        if (inboxRepository.insertIfNotExists(UUID.fromString(idempotenceId), consumerGroup)) {
            action()
        } else {
            logger.info("Skipping duplicate event with idempotenceId $idempotenceId and consumerGroup $consumerGroup")
        }
    }
}
