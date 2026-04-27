package ch.ejpd.example.precrime.infrastructure.integration.persistence

import org.jooq.DSLContext
import org.jooq.JSON
import org.jooq.Record
import org.jooq.impl.DSL
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import java.time.OffsetDateTime
import java.util.*

@Component
class JooqOutboxRepository(
    private val dsl: DSLContext,
    private val objectMapper: ObjectMapper
) {
    private val OUTBOX_TABLE = DSL.table("outbox")
    private val ID = uuidField("id", OutboxId::class.java, ::OutboxId, OutboxId::value)
    private val EVENT_CLASS = DSL.field("event_class", String::class.java)
    private val EVENT = DSL.field("event", JSON::class.java)
    private val STATUS = enumField("status", OutboxState::class.java)
    private val CREATED_AT = DSL.field("created_at", OffsetDateTime::class.java)
    private val PROCESSED_AT = DSL.field("processed_at", OffsetDateTime::class.java)

    fun create(event: Any): OutboxId {

        val id = OutboxId()
        dsl.insertInto(OUTBOX_TABLE)
            .set(ID, id)
            .set(CREATED_AT, OffsetDateTime.now())
            .set(EVENT_CLASS, event::class.java.name)
            .set(EVENT, JSON.json(objectMapper.writeValueAsString(event)))
            .set(STATUS, OutboxState.PENDING)
            .execute()
        return id
    }

    fun findById(id: OutboxId): OutboxRecord? {
        return dsl.select(ID, EVENT_CLASS, EVENT, STATUS)
            .from(OUTBOX_TABLE)
            .where(ID.eq(id))
            .fetchOne()
            ?.map { it.toOutboxRecord() }
    }

    fun findPendingForUpdate(): List<OutboxRecord> {
        return dsl.select(ID, EVENT_CLASS, EVENT, STATUS)
            .from(OUTBOX_TABLE)
            .where(STATUS.eq(OutboxState.PENDING))
            .forUpdate()
            .skipLocked()
            .fetch()
            .map { it.toOutboxRecord() }
    }

    fun markAsProcessed(id: OutboxId) {
        dsl.update(OUTBOX_TABLE)
            .set(STATUS, OutboxState.PROCESSED)
            .set(PROCESSED_AT, OffsetDateTime.now())
            .where(ID.eq(id))
            .execute()
    }

    private fun Record.toOutboxRecord() = OutboxRecord(
        id = get(ID),
        event = convertToEvent(get(EVENT).data(), Class.forName(get(EVENT_CLASS))),
        status = get(STATUS)
    )

    private fun convertToEvent(json: String, clazz: Class<*>): Any {
        return objectMapper.readValue(json, clazz)
    }
}

@JvmInline
value class OutboxId(val value: UUID = UUID.randomUUID())

enum class OutboxState { PENDING, PROCESSED }

data class OutboxRecord(
    val id: OutboxId,
    val event: Any,
    val status: OutboxState
)