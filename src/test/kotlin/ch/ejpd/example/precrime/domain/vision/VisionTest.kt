package ch.ejpd.example.precrime.domain.vision

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class VisionTest {

    private val vision = Vision(
        id = VisionId(),
        perpetrator = Perpetrator("John", "Doe"),
        crimeType = CrimeType.MURDER,
        foreseenAt = LocalDateTime.now()
    )

    @Test
    fun `foreseeCrime should record CrimeForeseenEvent`() {
        // WHEN
        val visionId = vision.foreseeCrime()

        // THEN
        assertThat(visionId).isNotNull
        assertThat(vision.domainEvents).hasSize(1)
        val event = vision.domainEvents.first() as CrimeForeseenEvent
        assertThat(event.visionId).isEqualTo(vision.id)
        assertThat(event.perpetrator).isEqualTo(vision.perpetrator)
        assertThat(event.crimeType).isEqualTo(vision.crimeType)
    }


}
