package ch.ejpd.example.precrime.application.event

interface DomainEventPublisher {
    fun publish(event: Any)
}
