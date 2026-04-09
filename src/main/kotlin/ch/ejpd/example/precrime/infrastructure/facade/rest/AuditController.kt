package ch.ejpd.example.precrime.infrastructure.facade.rest

import ch.ejpd.example.precrime.application.AuditApplicationService
import ch.ejpd.example.precrime.domain.audit.AuditEntry
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/audit")
class AuditController(private val auditService: AuditApplicationService) {

    @GetMapping("/logs")
    fun getLogs(): List<AuditEntry> {
        return auditService.getAuditLogs()
    }
}
