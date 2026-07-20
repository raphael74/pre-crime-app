package org.example.precrime.infrastructure.integration.persistence

import org.example.precrime.domain.audit.AuditEntry
import org.example.precrime.domain.audit.AuditEntryRepository
import org.example.precrime.infrastructure.integration.persistence.jooq.tables.references.AUDIT_LOG
import org.jooq.DSLContext
import org.jooq.JSON
import org.jooq.Record
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class JooqAuditEntryRepository(private val dsl: DSLContext) : AuditEntryRepository {

    @Transactional(propagation = Propagation.MANDATORY)
    override fun create(auditEntry: AuditEntry) {
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
            .map { it.toAuditEntry() }
    }

    private fun Record.toAuditEntry() = AuditEntry(
        id = get(AUDIT_LOG.ID)!!,
        eventType = get(AUDIT_LOG.EVENT_TYPE)!!,
        payload = get(AUDIT_LOG.PAYLOAD)!!.data(),
        recordedAt = get(AUDIT_LOG.RECORDED_AT)!!
    )
}
