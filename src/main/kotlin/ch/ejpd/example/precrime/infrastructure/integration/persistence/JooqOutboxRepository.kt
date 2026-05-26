package ch.ejpd.example.precrime.infrastructure.integration.persistence

import ch.ejpd.example.precrime.infrastructure.integration.persistence.jooq.tables.references.OUTBOX
import org.jooq.DSLContext
import org.jooq.JSON
import org.jooq.Record
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper
import java.time.OffsetDateTime
import java.util.*

@Component
class JooqOutboxRepository(
    private val dsl: DSLContext,
    private val objectMapper: ObjectMapper
) {
    @Transactional(propagation = Propagation.MANDATORY)
    fun create(event: Any): OutboxId {

        val id = OutboxId()
        dsl.insertInto(OUTBOX)
            .set(OUTBOX.ID, id)
            .set(OUTBOX.CREATED_AT, OffsetDateTime.now())
            .set(OUTBOX.EVENT_CLASS, event::class.java.name)
            .set(OUTBOX.EVENT, JSON.json(objectMapper.writeValueAsString(event)))
            .set(OUTBOX.STATUS, OutboxState.PENDING)
            .execute()
        return id
    }

    fun findById(id: OutboxId): OutboxRecord? {
        return dsl.select(OUTBOX.ID, OUTBOX.EVENT_CLASS, OUTBOX.EVENT, OUTBOX.STATUS)
            .from(OUTBOX)
            .where(OUTBOX.ID.eq(id))
            .fetchOne()
            ?.toOutboxRecord()
    }

    fun findPendingForUpdate(): List<OutboxRecord> {
        return dsl.select(OUTBOX.ID, OUTBOX.EVENT_CLASS, OUTBOX.EVENT, OUTBOX.STATUS)
            .from(OUTBOX)
            .where(OUTBOX.STATUS.eq(OutboxState.PENDING))
            .forUpdate()
            .skipLocked()
            .fetch()
            .map { it.toOutboxRecord() }
    }

    @Transactional(propagation = Propagation.MANDATORY)
    fun markAsProcessed(id: OutboxId) {
        dsl.update(OUTBOX)
            .set(OUTBOX.STATUS, OutboxState.PROCESSED)
            .set(OUTBOX.PROCESSED_AT, OffsetDateTime.now())
            .where(OUTBOX.ID.eq(id))
            .execute()
    }

    private fun Record.toOutboxRecord() = OutboxRecord(
        id = get(OUTBOX.ID)!!,
        event = convertToEvent(get(OUTBOX.EVENT)!!.data(), Class.forName(get(OUTBOX.EVENT_CLASS)!!)),
        status = get(OUTBOX.STATUS)!!
    )

    private fun convertToEvent(json: String, clazz: Class<*>): Any {
        return objectMapper.readValue(json, clazz)
    }
}

data class OutboxId(val value: UUID = UUID.randomUUID())

enum class OutboxState { PENDING, PROCESSED }

data class OutboxRecord(
    val id: OutboxId,
    val event: Any,
    val status: OutboxState
)