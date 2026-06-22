package ch.ejpd.example.precrime.infrastructure.facade.web

import ch.ejpd.example.precrime.application.AuditApplicationService
import ch.ejpd.example.precrime.application.PreCrimeApplicationService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import tools.jackson.databind.ObjectMapper

@Controller
@RequestMapping("/fragments")
class FragmentController(
    private val applicationService: PreCrimeApplicationService,
    private val auditService: AuditApplicationService,
    private val objectMapper: ObjectMapper
) {

    @GetMapping("/audit-logs")
    fun auditLogs(model: Model): String {
        val logs = auditService.getAuditLogs().sortedByDescending { it.recordedAt }.map {
            it.copy(payload = formatPayload(it.payload))
        }
        model.addAttribute("logs", logs)
        return "fragments/audit-logs"
    }

    private fun formatPayload(payload: String): String {
        return try {
            val json = objectMapper.readTree(payload)
            objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json)
        } catch (e: Exception) {
            payload
        }
    }

    @GetMapping("/pending-pre-arrests")
    fun pendingPreArrests(model: Model): String {
        val arrests = applicationService.getAllPendingPreArrests()
        model.addAttribute("pendingPreArrests", arrests)
        return "fragments/pending-pre-arrests"
    }

    @GetMapping("/executed-pre-arrests")
    fun executedPreArrests(model: Model): String {
        val arrests = applicationService.getAllExecutedPreArrests()
        model.addAttribute("executedPreArrests", arrests)
        return "fragments/executed-pre-arrests"
    }

    @GetMapping("/apologies")
    fun apologies(model: Model): String {
        val apologies = applicationService.getAllApologies()
        model.addAttribute("apologies", apologies)
        return "fragments/apologies"
    }

    @GetMapping("/stats")
    fun stats(model: Model): String {
        val count = applicationService.getPreventedCrimesCount()
        model.addAttribute("crimesPrevented", count)
        return "fragments/stats"
    }
}
