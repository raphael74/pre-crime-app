package ch.ejpd.example.precrime.infrastructure.integration.event

interface OutboxRepository {
    fun create(event: Any): OutboxId
    fun findById(id: OutboxId): Outbox?
    fun findPendingForUpdate(): List<Outbox>
    fun markAsProcessed(id: OutboxId)
    fun markAsInvalid(id: OutboxId)
}