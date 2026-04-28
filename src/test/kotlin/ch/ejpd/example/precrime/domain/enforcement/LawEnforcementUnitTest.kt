package ch.ejpd.example.precrime.domain.enforcement

import ch.ejpd.example.precrime.domain.precog.Perpetrator
import ch.ejpd.example.precrime.domain.precog.VisionId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LawEnforcementUnitTest {

    private val unit = LawEnforcementUnit()

    @Test
    fun `executePreArrest should add PreArrest to unit and record event`() {
        // GIVEN
        val visionId = VisionId()
        val perpetrator = Perpetrator("John Doe")

        // WHEN
        unit.executePreArrest(visionId, perpetrator)

        // THEN
        assertThat(unit.preArrests).hasSize(1)
        val preArrest = unit.preArrests.first()
        assertThat(preArrest.visionId).isEqualTo(visionId)
        assertThat(preArrest.perpetrator).isEqualTo(perpetrator)
        assertThat(preArrest.status).isEqualTo(PreArrestStatus.ARRESTED_BEFORE_CRIME)

        assertThat(unit.domainEvents).hasSize(1)
        val event = unit.domainEvents.first() as PreArrestExecutedEvent
        assertThat(event.visionId).isEqualTo(visionId)
        assertThat(event.perpetrator).isEqualTo(perpetrator)
    }
}
