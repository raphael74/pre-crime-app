package ch.ejpd.example.precrime

import ch.ejpd.example.precrime.domain.perpetrator.Perpetrator
import ch.ejpd.example.precrime.domain.perpetrator.PerpetratorRepository
import ch.ejpd.example.precrime.domain.prearrest.PreArrestRepository
import ch.ejpd.example.precrime.domain.vision.CrimeForeseenEvent
import ch.ejpd.example.precrime.domain.vision.CrimeType
import ch.ejpd.example.precrime.domain.vision.VisionId
import ch.ejpd.example.precrime.infrastructure.facade.event.KafkaDomainEventConsumer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import java.util.*

@IntegrationTest
@org.springframework.transaction.annotation.Transactional
class IdempotenceIntegrationTest(
    @Autowired private val consumer: KafkaDomainEventConsumer,
    @Autowired private val preArrestRepository: PreArrestRepository,
    @Autowired private val perpetratorRepository: PerpetratorRepository
) {

    @Test
    fun `duplicate events should be processed only once`() {
        // GIVEN
        val visionId = VisionId()
        val perpetrator = Perpetrator(firstName = "Unknown", lastName = "Suspect")
        perpetratorRepository.save(perpetrator)

        val event = CrimeForeseenEvent(visionId, perpetrator.id, CrimeType.THEFT, LocalDateTime.now())
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
