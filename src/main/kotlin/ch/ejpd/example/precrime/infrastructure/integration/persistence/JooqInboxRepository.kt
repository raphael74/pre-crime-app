package ch.ejpd.example.precrime.infrastructure.integration.persistence

import ch.ejpd.example.precrime.infrastructure.facade.event.InboxRepository
import ch.ejpd.example.precrime.infrastructure.integration.persistence.jooq.tables.references.INBOX
import org.jooq.DSLContext
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.util.*

@Component
class JooqInboxRepository(
    private val dsl: DSLContext
) : InboxRepository {

    override fun insertIfNotExists(id: UUID, consumerGroup: String): Boolean {
        // Use a manual check + insert to be safe with H2's problematic MERGE/ON CONFLICT support
        // since we are in a transaction anyway.
        val exists = dsl.fetchExists(
            dsl.selectOne()
                .from(INBOX)
                .where(INBOX.ID.eq(id))
                .and(INBOX.CONSUMER_GROUP.eq(consumerGroup))
        )

        return !exists && try {
            dsl.insertInto(INBOX)
                .set(INBOX.ID, id)
                .set(INBOX.CONSUMER_GROUP, consumerGroup)
                .set(INBOX.PROCESSED_AT, OffsetDateTime.now())
                .execute()
            true
        } catch (e: Exception) {
            // In case of race condition between fetchExists and insert
            false
        }
    }
}
