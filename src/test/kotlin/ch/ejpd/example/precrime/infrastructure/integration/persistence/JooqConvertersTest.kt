package ch.ejpd.example.precrime.infrastructure.integration.persistence

import ch.ejpd.example.precrime.domain.AggregateVersion
import ch.ejpd.example.precrime.domain.audit.AuditEntryId
import ch.ejpd.example.precrime.domain.perpetrator.PerpetratorId
import ch.ejpd.example.precrime.domain.preapology.PreApologyId
import ch.ejpd.example.precrime.domain.prearrest.PreArrestId
import ch.ejpd.example.precrime.domain.statistic.StatisticId
import ch.ejpd.example.precrime.domain.vision.VisionId
import ch.ejpd.example.precrime.infrastructure.integration.event.OutboxId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

class JooqConvertersTest {

    @Test
    fun `AggregateVersionConverter should convert Long to AggregateVersion and back`() {
        val converter = AggregateVersionConverter()
        assertThat(converter.from(5L)).isEqualTo(AggregateVersion(5))
        assertThat(converter.to(AggregateVersion(5))).isEqualTo(5L)
        assertThat(converter.from(null)).isNull()
        assertThat(converter.to(null)).isNull()
    }

    @Test
    fun `StatisticIdConverter should convert UUID to StatisticId and back`() {
        val converter = StatisticIdConverter()
        val uuid = UUID.randomUUID()
        assertThat(converter.from(uuid)).isEqualTo(StatisticId(uuid))
        assertThat(converter.to(StatisticId(uuid))).isEqualTo(uuid)
        assertThat(converter.from(null)).isNull()
        assertThat(converter.to(null)).isNull()
    }

    @Test
    fun `VisionIdConverter should convert UUID to VisionId and back`() {
        val converter = VisionIdConverter()
        val uuid = UUID.randomUUID()
        assertThat(converter.from(uuid)).isEqualTo(VisionId(uuid))
        assertThat(converter.to(VisionId(uuid))).isEqualTo(uuid)
        assertThat(converter.from(null)).isNull()
        assertThat(converter.to(null)).isNull()
    }

    @Test
    fun `PreArrestIdConverter should convert UUID to PreArrestId and back`() {
        val converter = PreArrestIdConverter()
        val uuid = UUID.randomUUID()
        assertThat(converter.from(uuid)).isEqualTo(PreArrestId(uuid))
        assertThat(converter.to(PreArrestId(uuid))).isEqualTo(uuid)
        assertThat(converter.from(null)).isNull()
        assertThat(converter.to(null)).isNull()
    }

    @Test
    fun `AuditEntryIdConverter should convert UUID to AuditEntryId and back`() {
        val converter = AuditEntryIdConverter()
        val uuid = UUID.randomUUID()
        assertThat(converter.from(uuid)).isEqualTo(AuditEntryId(uuid))
        assertThat(converter.to(AuditEntryId(uuid))).isEqualTo(uuid)
        assertThat(converter.from(null)).isNull()
        assertThat(converter.to(null)).isNull()
    }

    @Test
    fun `OutboxIdConverter should convert UUID to OutboxId and back`() {
        val converter = OutboxIdConverter()
        val uuid = UUID.randomUUID()
        assertThat(converter.from(uuid)).isEqualTo(OutboxId(uuid))
        assertThat(converter.to(OutboxId(uuid))).isEqualTo(uuid)
        assertThat(converter.from(null)).isNull()
        assertThat(converter.to(null)).isNull()
    }

    @Test
    fun `PreApologyIdConverter should convert UUID to PreApologyId and back`() {
        val converter = PreApologyIdConverter()
        val uuid = UUID.randomUUID()
        assertThat(converter.from(uuid)).isEqualTo(PreApologyId(uuid))
        assertThat(converter.to(PreApologyId(uuid))).isEqualTo(uuid)
        assertThat(converter.from(null)).isNull()
        assertThat(converter.to(null)).isNull()
    }

    @Test
    fun `PerpetratorIdConverter should convert UUID to PerpetratorId and back`() {
        val converter = PerpetratorIdConverter()
        val uuid = UUID.randomUUID()
        assertThat(converter.from(uuid)).isEqualTo(PerpetratorId(uuid))
        assertThat(converter.to(PerpetratorId(uuid))).isEqualTo(uuid)
        assertThat(converter.from(null)).isNull()
        assertThat(converter.to(null)).isNull()
    }
}
