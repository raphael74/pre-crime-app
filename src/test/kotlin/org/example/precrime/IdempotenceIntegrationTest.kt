package org.example.precrime

import org.example.precrime.domain.perpetrator.Perpetrator
import org.example.precrime.domain.perpetrator.PerpetratorRepository
import org.example.precrime.domain.prearrest.PreArrestRepository
import org.example.precrime.domain.vision.*
import org.example.precrime.infrastructure.facade.event.KafkaDomainEventConsumer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.OffsetDateTime
import java.util.*

@IntegrationTest
@org.springframework.transaction.annotation.Transactional
class IdempotenceIntegrationTest(
    @Autowired private val consumer: KafkaDomainEventConsumer,
    @Autowired private val preArrestRepository: PreArrestRepository,
    @Autowired private val perpetratorRepository: PerpetratorRepository,
    @Autowired private val visionRepository: VisionRepository
) {

    @Test
    fun `duplicate events should be processed only once`() {
        // GIVEN
        val visionId = VisionId()
        val perpetrator = Perpetrator(firstName = "Unknown", lastName = "Suspect")
        perpetratorRepository.create(perpetrator)
        val vision = Vision(
            id = visionId,
            perpetratorId = perpetrator.id,
            crimeType = CrimeType.THEFT,
            foreseenAt = OffsetDateTime.now()
        )
        visionRepository.create(vision)

        val event = CrimeForeseenEvent(visionId, perpetrator.id, CrimeType.THEFT, OffsetDateTime.now())
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
