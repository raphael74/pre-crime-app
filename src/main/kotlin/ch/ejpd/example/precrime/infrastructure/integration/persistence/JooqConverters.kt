package ch.ejpd.example.precrime.infrastructure.integration.persistence

import ch.ejpd.example.precrime.domain.AggregateVersion
import ch.ejpd.example.precrime.domain.audit.AuditEntryId
import ch.ejpd.example.precrime.domain.enforcement.EnforcementUnitId
import ch.ejpd.example.precrime.domain.enforcement.PreArrestId
import ch.ejpd.example.precrime.domain.precog.PrecogDivisionId
import ch.ejpd.example.precrime.domain.precog.VisionId
import org.jooq.impl.AbstractConverter
import java.util.*

class AggregateVersionConverter :
    AbstractConverter<Long, AggregateVersion>(Long::class.javaObjectType, AggregateVersion::class.java) {
    override fun from(t: Long?): AggregateVersion? = t?.let { AggregateVersion(it) }
    override fun to(u: AggregateVersion?): Long? = u?.value
}

class PrecogDivisionIdConverter :
    AbstractConverter<UUID, PrecogDivisionId>(UUID::class.java, PrecogDivisionId::class.java) {
    override fun from(t: UUID?): PrecogDivisionId? = t?.let { PrecogDivisionId(it) }
    override fun to(u: PrecogDivisionId?): UUID? = u?.value
}

class VisionIdConverter : AbstractConverter<UUID, VisionId>(UUID::class.java, VisionId::class.java) {
    override fun from(t: UUID?): VisionId? = t?.let { VisionId(it) }
    override fun to(u: VisionId?): UUID? = u?.value
}

class EnforcementUnitIdConverter :
    AbstractConverter<UUID, EnforcementUnitId>(UUID::class.java, EnforcementUnitId::class.java) {
    override fun from(t: UUID?): EnforcementUnitId? = t?.let { EnforcementUnitId(it) }
    override fun to(u: EnforcementUnitId?): UUID? = u?.value
}

class PreArrestIdConverter : AbstractConverter<UUID, PreArrestId>(UUID::class.java, PreArrestId::class.java) {
    override fun from(t: UUID?): PreArrestId? = t?.let { PreArrestId(it) }
    override fun to(u: PreArrestId?): UUID? = u?.value
}

class AuditEntryIdConverter : AbstractConverter<UUID, AuditEntryId>(UUID::class.java, AuditEntryId::class.java) {
    override fun from(t: UUID?): AuditEntryId? = t?.let { AuditEntryId(it) }
    override fun to(u: AuditEntryId?): UUID? = u?.value
}

class OutboxIdConverter : AbstractConverter<UUID, OutboxId>(UUID::class.java, OutboxId::class.java) {
    override fun from(t: UUID?): OutboxId? = t?.let { OutboxId(it) }
    override fun to(u: OutboxId?): UUID? = u?.value
}
