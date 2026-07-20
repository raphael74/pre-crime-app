package org.example.precrime.domain.audit

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AuditEntryTest {

    @Test
    fun `should create audit entry with given event type and payload`() {
        val entry = AuditEntry(eventType = "VISION_DETECTED", payload = "{\"crime\":\"Murder\"}")

        assertThat(entry.eventType).isEqualTo("VISION_DETECTED")
        assertThat(entry.payload).isEqualTo("{\"crime\":\"Murder\"}")
        assertThat(entry.id).isNotNull()
        assertThat(entry.recordedAt).isNotNull()
    }

    @Test
    fun `should generate unique ids for each audit entry`() {
        val entry1 = AuditEntry(eventType = "EVENT_A", payload = "data1")
        val entry2 = AuditEntry(eventType = "EVENT_B", payload = "data2")

        assertThat(entry1.id).isNotEqualTo(entry2.id)
    }
}
