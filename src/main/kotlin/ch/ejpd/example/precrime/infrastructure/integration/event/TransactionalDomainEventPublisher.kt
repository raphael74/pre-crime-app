package ch.ejpd.example.precrime.infrastructure.integration.event

import ch.ejpd.example.precrime.domain.DomainEventPublisher
import ch.ejpd.example.precrime.domain.enforcement.PreArrestExecutedEvent
import ch.ejpd.example.precrime.domain.precog.CrimeForeseenEvent
import ch.ejpd.example.precrime.infrastructure.KafkaTopics.Companion.CRIME_FORESEEN_EVENT_TOPIC
import ch.ejpd.example.precrime.infrastructure.KafkaTopics.Companion.PRE_ARREST_EXECUTED_EVENT_TOPIC
import ch.ejpd.example.precrime.infrastructure.integration.persistence.JooqOutboxRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper

@Component
class TransactionalDomainEventPublisher(
    private val outboxRepository: JooqOutboxRepository,
    private val objectMapper: ObjectMapper
) : DomainEventPublisher {
    private val logger = LoggerFactory.getLogger(javaClass)

    @org.jmolecules.event.annotation.DomainEventPublisher
    @Transactional(propagation = Propagation.MANDATORY)
    override fun publish(event: Any) {
        if (event is Iterable<*>) {
            event.filterNotNull().forEach { publishSingle(it) }
        } else {
            publishSingle(event)
        }
    }

    private fun publishSingle(event: Any) {
        var eventType: String
        var eventKey: String
        var topic: String

        when (event) {
            is CrimeForeseenEvent -> {
                eventType = "CrimeForeseenEvent"
                eventKey = event.visionId.value.toString()
                topic = CRIME_FORESEEN_EVENT_TOPIC
            }

            is PreArrestExecutedEvent -> {
                eventType = "PreArrestExecutedEvent"
                eventKey = event.preArrestId.value.toString()
                topic = PRE_ARREST_EXECUTED_EVENT_TOPIC
            }

            else -> throw IllegalArgumentException("Unsupported event type: ${event::class.simpleName}")
        }

        val payload = objectMapper.writeValueAsString(event)

        logger.info("Adding event $eventType to Outbox for topic $topic with key $eventKey")
        outboxRepository.create(eventType, topic, eventKey, payload)
    }
}
