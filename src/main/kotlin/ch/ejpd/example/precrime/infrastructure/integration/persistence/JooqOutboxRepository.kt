package ch.ejpd.example.precrime.infrastructure.integration.persistence

import org.jooq.DSLContext
import org.jooq.JSON
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
    private val TOPIC = DSL.field("topic", String::class.java)
    private val EVENT_KEY = DSL.field("event_key", String::class.java)
    private val PAYLOAD = DSL.field("payload", JSON::class.java)
    private val STATUS = DSL.field("status", String::class.java)
    private val CREATED_AT = DSL.field("created_at", OffsetDateTime::class.java)
    private val PROCESSED_AT = DSL.field("processed_at", OffsetDateTime::class.java)

    fun create(eventType: String, topic: String, eventKey: String, payload: String): OutboxId {
        val id = OutboxId()
        dsl.insertInto(OUTBOX_TABLE)
            .set(ID, id.value)
            .set(EVENT_TYPE, eventType)
            .set(TOPIC, topic)
            .set(EVENT_KEY, eventKey)
            .set(PAYLOAD, JSON.json(payload))
            .set(STATUS, OutboxState.PENDING.name)
            .execute()
        return id
    }

    fun findById(id: OutboxId): OutboxRecord? {
        return dsl.selectFrom(OUTBOX_TABLE)
            .where(ID.eq(id.value))
            .fetchOne()
            ?.map { it.toOutboxRecord() }
    }

    fun findPendingForUpdate(): List<OutboxRecord> {
        return dsl.selectFrom(OUTBOX_TABLE)
            .where(STATUS.eq(OutboxState.PENDING.name))
            .forUpdate()
            .skipLocked()
            .fetch()
            .map { it.toOutboxRecord() }
    }

    fun markAsProcessed(id: OutboxId) {
        dsl.update(OUTBOX_TABLE)
            .set(STATUS, OutboxState.PROCESSED.name)
            .set(PROCESSED_AT, OffsetDateTime.now())
            .where(ID.eq(id.value))
            .execute()
    }

    private fun Record.toOutboxRecord() = OutboxRecord(
        id = OutboxId(get(ID)),
        eventType = get(EVENT_TYPE),
        topic = get(TOPIC),
        eventKey = get(EVENT_KEY),
        payload = get(PAYLOAD).data(),
        status = OutboxState.valueOf(get(STATUS))
    )
}

@JvmInline
value class OutboxId(val value: UUID = UUID.randomUUID())

enum class OutboxState { PENDING, PROCESSED }

data class OutboxRecord(
    val id: OutboxId,
    val eventType: String,
    val topic: String,
    val eventKey: String,
    val payload: String,
    val status: OutboxState
)