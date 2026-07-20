package org.example.precrime.infrastructure.facade.rest

import org.example.precrime.application.AuditApplicationService
import org.example.precrime.infrastructure.facade.rest.api.AuditApi
import org.example.precrime.infrastructure.facade.rest.model.AuditEntry
import org.example.precrime.infrastructure.facade.rest.model.AuditEntryId
import org.example.precrime.infrastructure.facade.security.SecurityConfiguration.Companion.USER_ROLE
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class AuditController(private val auditService: AuditApplicationService) : AuditApi {

    @PreAuthorize("hasRole('$USER_ROLE')")
    override fun getLogs(): ResponseEntity<List<AuditEntry>> {
        val logs = auditService.getAuditLogs().map {
            AuditEntry(
                id = AuditEntryId(it.id.value),
                eventType = it.eventType,
                payload = it.payload,
                recordedAt = it.recordedAt
            )
        }
        return ResponseEntity.ok(logs)
    }
}
