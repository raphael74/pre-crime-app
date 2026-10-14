package org.example.precrime.infrastructure.facade.event

import java.util.UUID

interface InboxRepository {
    fun insertIfNotExists(
        id: UUID,
        consumerGroup: String,
    ): Boolean
}
