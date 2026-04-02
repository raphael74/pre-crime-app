package ch.ejpd.example.precrime.infrastructure.facade.event

import ch.ejpd.example.precrime.application.PreCrimeApplicationService
import ch.ejpd.example.precrime.domain.enforcement.PreArrestExecutedEvent
import ch.ejpd.example.precrime.domain.precog.CrimeForeseen
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

@Component
class KafkaDomainEventConsumer(
    private val applicationService: PreCrimeApplicationService,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = ["crime-foreseen"], groupId = "pre-crime-group")
    fun onCrimeForeseen(payload: String) {
        logger.info("📥 Received CrimeForeseen from Kafka")
        val event = objectMapper.readValue(payload, CrimeForeseen::class.java)
        applicationService.onCrimeForeseen(event)
    }

    @KafkaListener(topics = ["pre-arrest-executed"], groupId = "pre-crime-group")
    fun onPreArrestExecuted(payload: String) {
        logger.info("📥 Received PreArrestExecuted from Kafka")
        val event = objectMapper.readValue(payload, PreArrestExecutedEvent::class.java)
        applicationService.onPreArrestExecuted(event)
    }
}