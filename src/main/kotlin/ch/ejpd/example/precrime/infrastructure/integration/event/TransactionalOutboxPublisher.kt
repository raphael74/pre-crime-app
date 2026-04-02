package ch.ejpd.example.precrime.infrastructure.integration.event

import ch.ejpd.example.precrime.domain.DomainEventPublisher
import ch.ejpd.example.precrime.infrastructure.integration.persistence.JooqOutboxRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper

@Component
class TransactionalOutboxPublisher(
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
        val payload = objectMapper.writeValueAsString(event)
        val eventType = event::class.java.name

        logger.info("Adding event $eventType to Outbox")
        logger.debug(payload)
        outboxRepository.create(eventType, payload)
    }
}
