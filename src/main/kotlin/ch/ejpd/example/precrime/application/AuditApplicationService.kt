package ch.ejpd.example.precrime.application

import ch.ejpd.example.precrime.domain.audit.AuditEntry
import ch.ejpd.example.precrime.domain.audit.AuditEntryRepository
import org.jmolecules.event.annotation.DomainEventHandler
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@org.jmolecules.ddd.annotation.Service
@Service
@Transactional
class AuditApplicationService(
    private val auditEntryRepository: AuditEntryRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @DomainEventHandler
    fun logEvent(eventType: String, payload: String) {
        logger.info("🕵️ [Audit] Logging $eventType")
        val entry = AuditEntry(eventType = eventType, payload = payload)
        auditEntryRepository.save(entry)
    }

    @Transactional(readOnly = true)
    fun getAuditLogs(): List<AuditEntry> {
        return auditEntryRepository.findAll()
    }
}
