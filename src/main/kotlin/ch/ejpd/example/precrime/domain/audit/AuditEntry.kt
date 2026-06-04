package ch.ejpd.example.precrime.domain.audit

import org.jmolecules.ddd.annotation.AggregateRoot
import org.jmolecules.ddd.annotation.Identity
import org.jmolecules.ddd.annotation.Repository
import org.jmolecules.ddd.types.Identifier
import java.time.OffsetDateTime
import java.util.*

@AggregateRoot
data class AuditEntry(
    @Identity val id: AuditEntryId = AuditEntryId(),
    val eventType: String,
    val payload: String,
    val recordedAt: OffsetDateTime = OffsetDateTime.now()
)

@JvmInline
value class AuditEntryId(val value: UUID = UUID.randomUUID()) : Identifier

@Repository
interface AuditEntryRepository {
    fun save(auditEntry: AuditEntry)
    fun findAll(): List<AuditEntry>
}
