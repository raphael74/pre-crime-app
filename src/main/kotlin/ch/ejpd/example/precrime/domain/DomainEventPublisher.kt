package ch.ejpd.example.precrime.domain

interface DomainEventPublisher {
    /**
     * Publishes a domain event or a collection of domain events.
     * Different event types are automatically routed to separate topics.
     */
    @org.jmolecules.event.annotation.DomainEventPublisher
    fun publish(event: Any)
}
