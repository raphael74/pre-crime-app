package ch.ejpd.example.precrime.domain.audit

import org.jmolecules.ddd.annotation.Repository

@Repository
interface AuditEntryRepository {
    fun create(auditEntry: AuditEntry)
    fun findAll(): List<AuditEntry>
}