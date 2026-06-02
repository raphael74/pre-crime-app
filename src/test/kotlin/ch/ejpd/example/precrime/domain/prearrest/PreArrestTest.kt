package ch.ejpd.example.precrime.domain.prearrest

import ch.ejpd.example.precrime.domain.perpetrator.PerpetratorId
import ch.ejpd.example.precrime.domain.vision.VisionId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PreArrestTest {

    @Test
    fun `creating PreArrest should initialize with correct status and record event`() {
        // GIVEN
        val visionId = VisionId()
        val perpetratorId = PerpetratorId()

        // WHEN
        val preArrest = PreArrest(visionId = visionId, perpetratorId = perpetratorId)

        // THEN
        assertThat(preArrest.status).isEqualTo(PreArrestStatus.PENDING)
        assertThat(preArrest.visionId).isEqualTo(visionId)
        assertThat(preArrest.perpetratorId).isEqualTo(perpetratorId)

        assertThat(preArrest.domainEvents).hasSize(0)
    }


    @Test
    fun `executing PreArrest should change to correct status and record event`() {
        // GIVEN
        val visionId = VisionId()
        val perpetratorId = PerpetratorId()
        val preArrest = PreArrest(visionId = visionId, perpetratorId = perpetratorId)

        // WHEN
        preArrest.executePreArrest()

        // THEN
        assertThat(preArrest.status).isEqualTo(PreArrestStatus.ARRESTED_BEFORE_CRIME)
        assertThat(preArrest.visionId).isEqualTo(visionId)
        assertThat(preArrest.perpetratorId).isEqualTo(perpetratorId)
        assertThat { preArrest.preArrestDate }.isNotNull()

        assertThat(preArrest.domainEvents).hasSize(1)
        val event = preArrest.domainEvents.first() as PreArrestExecutedEvent
        assertThat(event.visionId).isEqualTo(visionId)
        assertThat(event.perpetratorId).isEqualTo(perpetratorId)
        assertThat(event.preArrestId).isEqualTo(preArrest.id)
    }
}
