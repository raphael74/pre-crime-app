package ch.ejpd.example.precrime.domain.enforcement

import ch.ejpd.example.precrime.domain.DomainEventPublisher
import ch.ejpd.example.precrime.domain.precog.VisionId
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LawEnforcementUnitTest {

    private val publisher = mockk<DomainEventPublisher>(relaxed = true)
    private val unit = LawEnforcementUnit()

    @Test
    fun `executePreArrest should add PreArrest to unit and publish event`() {
        // GIVEN
        unit.register(publisher)
        val visionId = VisionId()
        val perpetrator = "John Doe"

        // WHEN
        unit.executePreArrest(visionId, perpetrator)

        // THEN
        assertThat(unit.preArrests).hasSize(1)
        val preArrest = unit.preArrests.first()
        assertThat(preArrest.visionId).isEqualTo(visionId)
        assertThat(preArrest.perpetrator).isEqualTo(perpetrator)
        assertThat(preArrest.status).isEqualTo("ARRESTED_BEFORE_CRIME")

        verify {
            publisher.publish(match {
                it is PreArrestExecutedEvent &&
                        it.visionId == visionId &&
                        it.perpetrator == perpetrator
            })
        }
        confirmVerified(publisher)
    }
}
