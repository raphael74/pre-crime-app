package ch.ejpd.example.precrime.infrastructure.integration.persistence

import ch.ejpd.example.precrime.domain.audit.AuditEntry
import ch.ejpd.example.precrime.domain.audit.AuditEntryId
import ch.ejpd.example.precrime.domain.audit.AuditEntryRepository
import org.jooq.DSLContext
import org.jooq.JSON
import org.jooq.impl.DSL
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Component
class JooqAuditEntryRepository(private val dsl: DSLContext) : AuditEntryRepository {
    private val AUDIT_TABLE = DSL.table("audit_log")
    private val ID = uuidField("id", AuditEntryId::class.java, ::AuditEntryId, AuditEntryId::value)
    private val EVENT_TYPE = DSL.field("event_type", String::class.java)
    private val PAYLOAD = DSL.field("payload", JSON::class.java)
    private val RECORDED_AT = DSL.field("recorded_at", OffsetDateTime::class.java)

    @Transactional(propagation = Propagation.MANDATORY)
    override fun save(auditEntry: AuditEntry) {
        dsl.insertInto(AUDIT_TABLE)
            .set(ID, auditEntry.id)
            .set(EVENT_TYPE, auditEntry.eventType)
            .set(PAYLOAD, JSON.json(auditEntry.payload))
            .set(RECORDED_AT, auditEntry.recordedAt)
            .execute()
    }

    override fun findAll(): List<AuditEntry> {
        return dsl.select(ID, EVENT_TYPE, PAYLOAD, RECORDED_AT)
            .from(AUDIT_TABLE)
            .orderBy(RECORDED_AT.desc())
            .fetch()
            .map { r ->
                AuditEntry(
                    id = r.get(ID),
                    eventType = r.get(EVENT_TYPE),
                    payload = r.get(PAYLOAD).data(),
                    recordedAt = r.get(RECORDED_AT)
                )
            }
    }
}
