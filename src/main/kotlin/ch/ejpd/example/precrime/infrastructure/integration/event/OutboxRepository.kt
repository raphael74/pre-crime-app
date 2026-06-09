package ch.ejpd.example.precrime.infrastructure.integration.event

import java.util.*

interface OutboxRepository {
    fun create(event: Any): OutboxId
    fun findById(id: OutboxId): OutboxRecord?
    fun findPendingForUpdate(): List<OutboxRecord>
    fun markAsProcessed(id: OutboxId)
}

@JvmInline
value class OutboxId(val value: UUID = UUID.randomUUID())

enum class OutboxState { PENDING, PROCESSED }

data class OutboxRecord(
    val id: OutboxId,
    val event: Any,
    val status: OutboxState
)