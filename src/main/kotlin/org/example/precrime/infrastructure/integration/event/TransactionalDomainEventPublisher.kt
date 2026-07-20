package org.example.precrime.infrastructure.integration.event

import org.example.precrime.domain.DomainEventPublisher
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class TransactionalDomainEventPublisher(
    private val outboxRepository: OutboxRepository
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
        logger.info("Queuing event ${event::class.simpleName} for publishing in outbox")
        outboxRepository.create(event)
    }
}
