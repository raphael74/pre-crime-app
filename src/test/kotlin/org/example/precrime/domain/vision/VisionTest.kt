package org.example.precrime.domain.vision

import org.example.precrime.domain.DomainEventPublisher
import org.example.precrime.domain.perpetrator.PerpetratorId
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

class VisionTest {

    @Test
    fun `foreseeCrime should publish CrimeForeseenEvent`() {
        // GIVEN
        val publisher = mockk<DomainEventPublisher>(relaxed = true)
        val vision = Vision(
            id = VisionId(),
            perpetratorId = PerpetratorId(),
            crimeType = CrimeType.MURDER,
            foreseenAt = OffsetDateTime.now()
        ).apply { injectPublisher(publisher) }

        // WHEN
        vision.foreseeCrime()

        // THEN
        verify {
            publisher.publish(
                match<CrimeForeseenEvent> {
                    it.visionId == vision.id &&
                            it.perpetratorId == vision.perpetratorId &&
                            it.crimeType == vision.crimeType
                }
            )
        }
    }

    @Test
    fun `foreseeCrime should throw when publisher not injected`() {
        // GIVEN
        val vision = Vision(
            id = VisionId(),
            perpetratorId = PerpetratorId(),
            crimeType = CrimeType.MURDER,
            foreseenAt = OffsetDateTime.now()
        )

        // WHEN + THEN
        assertThatThrownBy { vision.foreseeCrime() }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("DomainEventPublisher has not been injected into Vision")
    }
}
