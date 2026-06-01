package ch.ejpd.example.precrime.domain.enforcement

import ch.ejpd.example.precrime.domain.vision.Perpetrator
import ch.ejpd.example.precrime.domain.vision.VisionId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PreArrestTest {

    @Test
    fun `creating PreArrest should initialize with correct status and record event`() {
        // GIVEN
        val visionId = VisionId()
        val perpetrator = Perpetrator("John", "Doe")

        // WHEN
        val preArrest = PreArrest(visionId = visionId, perpetrator = perpetrator)

        // THEN
        assertThat(preArrest.status).isEqualTo(PreArrestStatus.ARRESTED_BEFORE_CRIME)
        assertThat(preArrest.visionId).isEqualTo(visionId)
        assertThat(preArrest.perpetrator).isEqualTo(perpetrator)

        assertThat(preArrest.domainEvents).hasSize(1)
        val event = preArrest.domainEvents.first() as PreArrestExecutedEvent
        assertThat(event.visionId).isEqualTo(visionId)
        assertThat(event.perpetrator).isEqualTo(perpetrator)
        assertThat(event.preArrestId).isEqualTo(preArrest.id)
    }
}
