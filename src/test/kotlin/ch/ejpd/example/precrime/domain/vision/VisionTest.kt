package ch.ejpd.example.precrime.domain.vision

import ch.ejpd.example.precrime.domain.DomainEventPublisher
import ch.ejpd.example.precrime.domain.perpetrator.PerpetratorId
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class VisionTest {

    @Test
    fun `foreseeCrime should publish CrimeForeseenEvent`() {
        // GIVEN
        val publisher = mockk<DomainEventPublisher>(relaxed = true)
        val vision = Vision(
            id = VisionId(),
            perpetratorId = PerpetratorId(),
            crimeType = CrimeType.MURDER,
            foreseenAt = LocalDateTime.now(),
            publisher = publisher
        )

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
}
