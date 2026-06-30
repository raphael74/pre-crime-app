package ch.ejpd.example.precrime.infrastructure.integration.event

import java.util.*

data class Outbox(
    val id: OutboxId,
    val event: Any,
    val status: OutboxState
)

@JvmInline
value class OutboxId(val value: UUID = UUID.randomUUID())

enum class OutboxState { PENDING, PROCESSED, INVALID }