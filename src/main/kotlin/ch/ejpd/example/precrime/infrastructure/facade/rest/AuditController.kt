package ch.ejpd.example.precrime.infrastructure.facade.rest

import ch.ejpd.example.precrime.application.AuditApplicationService
import ch.ejpd.example.precrime.domain.audit.AuditEntry
import ch.ejpd.example.precrime.infrastructure.facade.security.SecurityConfiguration.Companion.USER_ROLE
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/audit")
class AuditController(private val auditService: AuditApplicationService) {

    @GetMapping("/logs")
    @PreAuthorize("hasRole('$USER_ROLE')")
    fun getLogs(): List<AuditEntry> {
        return auditService.getAuditLogs()
    }
}
