package ch.ejpd.example.precrime.infrastructure.integration.persistence

import ch.ejpd.example.precrime.domain.InboxRepository
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.util.*

@Component
class JooqInboxRepository(
    private val dsl: DSLContext
) : InboxRepository {
    private val INBOX_TABLE = DSL.table(DSL.name("inbox"))
    private val ID = DSL.field(DSL.name("inbox", "id"), UUID::class.java)
    private val CONSUMER_GROUP = DSL.field(DSL.name("inbox", "consumer_group"), String::class.java)
    private val PROCESSED_AT = DSL.field(DSL.name("inbox", "processed_at"), OffsetDateTime::class.java)

    override fun insertIfNotExists(id: UUID, consumerGroup: String): Boolean {
        // Use a manual check + insert to be safe with H2's problematic MERGE/ON CONFLICT support
        // since we are in a transaction anyway.
        val exists = dsl.fetchExists(
            dsl.selectOne()
                .from(INBOX_TABLE)
                .where(ID.eq(id))
                .and(CONSUMER_GROUP.eq(consumerGroup))
        )

        if (exists) return false

        return try {
            dsl.insertInto(INBOX_TABLE)
                .set(ID, id)
                .set(CONSUMER_GROUP, consumerGroup)
                .set(PROCESSED_AT, OffsetDateTime.now())
                .execute()
            true
        } catch (e: Exception) {
            // In case of race condition between fetchExists and insert
            false
        }
    }
}
