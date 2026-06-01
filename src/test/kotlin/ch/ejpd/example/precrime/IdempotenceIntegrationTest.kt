package ch.ejpd.example.precrime

import ch.ejpd.example.precrime.domain.enforcement.PreArrestRepository
import ch.ejpd.example.precrime.domain.vision.CrimeForeseenEvent
import ch.ejpd.example.precrime.domain.vision.CrimeType
import ch.ejpd.example.precrime.domain.vision.Perpetrator
import ch.ejpd.example.precrime.domain.vision.VisionId
import ch.ejpd.example.precrime.infrastructure.facade.event.KafkaDomainEventConsumer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import java.util.*

@IntegrationTest
class IdempotenceIntegrationTest(
    @Autowired private val consumer: KafkaDomainEventConsumer,
    @Autowired private val preArrestRepository: PreArrestRepository
) {

    @Test
    fun `duplicate events should be processed only once`() {
        // GIVEN
        val visionId = VisionId()
        val perpetrator = Perpetrator("Unknown", "Suspect")
        val event = CrimeForeseenEvent(visionId, perpetrator, CrimeType.THEFT, LocalDateTime.now())
        val idempotenceId = UUID.randomUUID().toString()

        // WHEN: Process the event for the first time
        consumer.onCrimeForeseen(event, idempotenceId)

        // THEN: One pre-arrest should be recorded
        val arrestsAfterFirst = preArrestRepository.findAll()
        val initialPreArrestsCount = arrestsAfterFirst.size
        assertThat(arrestsAfterFirst).anyMatch { it.visionId == visionId }

        // WHEN: Process the same event again with the same eventId
        consumer.onCrimeForeseen(event, idempotenceId)

        // THEN: No additional pre-arrest should be recorded
        val arrestsAfterSecond = preArrestRepository.findAll()
        assertThat(arrestsAfterSecond.size).isEqualTo(initialPreArrestsCount)
    }
}
