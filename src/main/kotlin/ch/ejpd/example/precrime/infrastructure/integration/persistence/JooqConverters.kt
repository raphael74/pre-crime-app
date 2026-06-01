package ch.ejpd.example.precrime.infrastructure.integration.persistence

import ch.ejpd.example.precrime.domain.AggregateVersion
import ch.ejpd.example.precrime.domain.apology.PreApologyId
import ch.ejpd.example.precrime.domain.audit.AuditEntryId
import ch.ejpd.example.precrime.domain.enforcement.PreArrestId
import ch.ejpd.example.precrime.domain.statistic.StatisticId
import ch.ejpd.example.precrime.domain.vision.VisionId
import org.jooq.impl.AbstractConverter
import java.util.*

class AggregateVersionConverter :
    AbstractConverter<Long, AggregateVersion>(Long::class.javaObjectType, AggregateVersion::class.java) {
    override fun from(t: Long?): AggregateVersion? = t?.let { AggregateVersion(it) }
    override fun to(u: AggregateVersion?): Long? = u?.value
}

class StatisticIdConverter :
    AbstractConverter<UUID, StatisticId>(UUID::class.java, StatisticId::class.java) {
    override fun from(t: UUID?): StatisticId? = t?.let { StatisticId(it) }
    override fun to(u: StatisticId?): UUID? = u?.value
}

class VisionIdConverter : AbstractConverter<UUID, VisionId>(UUID::class.java, VisionId::class.java) {
    override fun from(t: UUID?): VisionId? = t?.let { VisionId(it) }
    override fun to(u: VisionId?): UUID? = u?.value
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

class PreApologyIdConverter : AbstractConverter<UUID, PreApologyId>(UUID::class.java, PreApologyId::class.java) {
    override fun from(t: UUID?): PreApologyId? = t?.let { PreApologyId(it) }
    override fun to(u: PreApologyId?): UUID? = u?.value
}
