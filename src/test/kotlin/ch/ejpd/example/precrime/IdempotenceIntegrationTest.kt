package ch.ejpd.example.precrime

import ch.ejpd.example.precrime.domain.enforcement.LawEnforcementRepository
import ch.ejpd.example.precrime.domain.precog.CrimeForeseenEvent
import ch.ejpd.example.precrime.domain.precog.CrimeType
import ch.ejpd.example.precrime.domain.precog.Perpetrator
import ch.ejpd.example.precrime.domain.precog.VisionId
import ch.ejpd.example.precrime.infrastructure.facade.event.KafkaDomainEventConsumer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import java.util.*

@IntegrationTest
class IdempotenceIntegrationTest(
    @Autowired private val consumer: KafkaDomainEventConsumer,
    @Autowired private val enforcementRepository: LawEnforcementRepository
) {

    @Test
    fun `duplicate events should be processed only once`() {
        // GIVEN
        val visionId = VisionId()
        val perpetrator = Perpetrator("Suspect")
        val event = CrimeForeseenEvent(visionId, perpetrator, CrimeType("Theft"), LocalDateTime.now())
        val idempotenceId = UUID.randomUUID().toString()

        // WHEN: Process the event for the first time
        consumer.onCrimeForeseen(event, idempotenceId)

        // THEN: One pre-arrest should be recorded
        val unitAfterFirst = enforcementRepository.findSingleton()
        val initialPreArrestsCount = unitAfterFirst.preArrests.size
        assertThat(unitAfterFirst.preArrests).anyMatch { it.visionId == visionId }

        // WHEN: Process the same event again with the same eventId
        consumer.onCrimeForeseen(event, idempotenceId)

        // THEN: No additional pre-arrest should be recorded
        val unitAfterSecond = enforcementRepository.findSingleton()
        assertThat(unitAfterSecond.preArrests.size).isEqualTo(initialPreArrestsCount)
    }
}
