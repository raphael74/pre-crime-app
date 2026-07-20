package org.example.precrime.application

import org.example.precrime.domain.audit.AuditEntry
import org.example.precrime.domain.audit.AuditEntryRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AuditApplicationServiceTest {

    private val auditEntryRepository = mockk<AuditEntryRepository>()
    private val service = AuditApplicationService(auditEntryRepository)

    @Test
    fun `logEvent should create audit entry`() {
        every { auditEntryRepository.create(any()) } returns Unit

        service.logEvent("VISION_DETECTED", "{\"crime\":\"Murder\"}")

        verify {
            auditEntryRepository.create(match {
                it.eventType == "VISION_DETECTED" && it.payload == "{\"crime\":\"Murder\"}"
            })
        }
    }

    @Test
    fun `getAuditLogs should return all entries from repository`() {
        val entries = listOf(
            AuditEntry(eventType = "EVENT_A", payload = "data1"),
            AuditEntry(eventType = "EVENT_B", payload = "data2")
        )
        every { auditEntryRepository.findAll() } returns entries

        val result = service.getAuditLogs()

        assertThat(result).hasSize(2)
        assertThat(result).isEqualTo(entries)
        verify { auditEntryRepository.findAll() }
    }

    @Test
    fun `getAuditLogs should return empty list when no entries`() {
        every { auditEntryRepository.findAll() } returns emptyList()

        val result = service.getAuditLogs()

        assertThat(result).isEmpty()
    }
}
