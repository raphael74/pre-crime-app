package ch.ejpd.example.precrime.domain.vision

import ch.ejpd.example.precrime.domain.perpetrator.PerpetratorId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class VisionTest {

    private val vision = Vision(
        id = VisionId(),
        perpetratorId = PerpetratorId(),
        crimeType = CrimeType.MURDER,
        foreseenAt = LocalDateTime.now()
    )

    @Test
    fun `foreseeCrime should record CrimeForeseenEvent`() {
        // WHEN
        vision.foreseeCrime()

        // THEN
        assertThat(vision.domainEvents).hasSize(1)
        val event = vision.domainEvents.first() as CrimeForeseenEvent
        assertThat(event.visionId).isEqualTo(vision.id)
        assertThat(event.perpetratorId).isEqualTo(vision.perpetratorId)
        assertThat(event.crimeType).isEqualTo(vision.crimeType)
    }


}
