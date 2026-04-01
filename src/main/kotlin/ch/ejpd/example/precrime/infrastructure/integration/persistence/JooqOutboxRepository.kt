package ch.ejpd.example.precrime.infrastructure.integration.persistence

import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.util.*

@Component
class JooqOutboxRepository(private val dsl: DSLContext) {
    private val OUTBOX_TABLE = DSL.table("outbox")
    private val ID = DSL.field("id", UUID::class.java)
    private val EVENT_TYPE = DSL.field("event_type", String::class.java)
    private val PAYLOAD = DSL.field("payload", String::class.java)
    private val STATUS = DSL.field("status", String::class.java)
    private val CREATED_AT = DSL.field("created_at", OffsetDateTime::class.java)
    private val PROCESSED_AT = DSL.field("processed_at", OffsetDateTime::class.java)

    fun create(eventType: String, payload: String): UUID {
        val id = UUID.randomUUID()
        dsl.insertInto(OUTBOX_TABLE)
            .set(ID, id)
            .set(EVENT_TYPE, eventType)
            .set(PAYLOAD, payload)
            .set(STATUS, "PENDING")
            .execute()
        return id
    }

    fun findById(id: UUID): OutboxRecord? {
        return dsl.selectFrom(OUTBOX_TABLE)
            .where(ID.eq(id))
            .fetchSingle()
            .map { it.toOutboxRecord() }
    }

    fun findPendingForUpdate(): List<OutboxRecord> {
        return dsl.selectFrom(OUTBOX_TABLE)
            .where(STATUS.eq("PENDING"))
            .forUpdate()
            .skipLocked()
            .fetch()
            .map { it.toOutboxRecord() }
    }

    fun markAsProcessed(id: UUID) {
        dsl.update(OUTBOX_TABLE)
            .set(STATUS, "PROCESSED")
            .set(PROCESSED_AT, OffsetDateTime.now())
            .where(ID.eq(id))
            .execute()
    }

    private fun Record.toOutboxRecord() = OutboxRecord(
        id = get(ID),
        eventType = get(EVENT_TYPE),
        payload = get(PAYLOAD)
    )
}

data class OutboxRecord(
    val id: UUID,
    val eventType: String,
    val payload: String
)