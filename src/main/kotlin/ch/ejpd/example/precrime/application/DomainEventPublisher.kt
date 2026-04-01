package ch.ejpd.example.precrime.application

interface DomainEventPublisher {
    /**
     * Publishes a domain event or a collection of domain events.
     * Different event types are automatically routed to separate topics.
     */
    fun publish(event: Any)
}