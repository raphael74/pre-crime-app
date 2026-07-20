package org.example.precrime.infrastructure.facade.event

import java.util.*

interface InboxRepository {
    fun insertIfNotExists(id: UUID, consumerGroup: String): Boolean
}