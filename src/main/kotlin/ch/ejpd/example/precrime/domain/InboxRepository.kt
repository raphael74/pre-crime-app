package ch.ejpd.example.precrime.domain

import org.jmolecules.ddd.annotation.Repository
import java.util.*

@Repository
interface InboxRepository {
    fun insertIfNotExists(id: UUID, consumerGroup: String): Boolean
}
