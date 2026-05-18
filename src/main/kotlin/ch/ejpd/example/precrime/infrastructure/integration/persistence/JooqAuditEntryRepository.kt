package ch.ejpd.example.precrime.infrastructure.integration.persistence

import ch.ejpd.example.precrime.domain.audit.AuditEntry
import ch.ejpd.example.precrime.domain.audit.AuditEntryRepository
import ch.ejpd.example.precrime.infrastructure.integration.persistence.jooq.tables.references.AUDIT_LOG
import org.jooq.DSLContext
import org.jooq.JSON
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class JooqAuditEntryRepository(private val dsl: DSLContext) : AuditEntryRepository {

    @Transactional(propagation = Propagation.MANDATORY)
    override fun save(auditEntry: AuditEntry) {
        dsl.insertInto(AUDIT_LOG)
            .set(AUDIT_LOG.ID, auditEntry.id)
            .set(AUDIT_LOG.EVENT_TYPE, auditEntry.eventType)
            .set(AUDIT_LOG.PAYLOAD, JSON.json(auditEntry.payload))
            .set(AUDIT_LOG.RECORDED_AT, auditEntry.recordedAt)
            .execute()
    }

    override fun findAll(): List<AuditEntry> {
        return dsl.select(AUDIT_LOG.ID, AUDIT_LOG.EVENT_TYPE, AUDIT_LOG.PAYLOAD, AUDIT_LOG.RECORDED_AT)
            .from(AUDIT_LOG)
            .orderBy(AUDIT_LOG.RECORDED_AT.desc())
            .fetch()
            .map { r ->
                AuditEntry(
                    id = r.get(AUDIT_LOG.ID)!!,
                    eventType = r.get(AUDIT_LOG.EVENT_TYPE)!!,
                    payload = r.get(AUDIT_LOG.PAYLOAD)!!.data(),
                    recordedAt = r.get(AUDIT_LOG.RECORDED_AT)!!
                )
            }
    }
}
