package org.example.precrime.domain.prearrest

import org.example.precrime.domain.DomainEventPublisher
import org.example.precrime.domain.perpetrator.PerpetratorId
import org.example.precrime.domain.vision.VisionId
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class PreArrestTest {

    @Test
    fun `creating PreArrest should initialize with correct status`() {
        // GIVEN
        val visionId = VisionId()
        val perpetratorId = PerpetratorId()

        // WHEN
        val preArrest = PreArrest(visionId = visionId, perpetratorId = perpetratorId)

        // THEN
        assertThat(preArrest.status).isEqualTo(PreArrestStatus.PENDING)
        assertThat(preArrest.visionId).isEqualTo(visionId)
        assertThat(preArrest.perpetratorId).isEqualTo(perpetratorId)
    }

    @Test
    fun `executePreArrest should throw when publisher not injected`() {
        // GIVEN
        val preArrest = PreArrest(visionId = VisionId(), perpetratorId = PerpetratorId())

        // WHEN + THEN
        assertThatThrownBy { preArrest.executePreArrest() }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("DomainEventPublisher has not been injected into PreArrest")
    }

    @Test
    fun `executing PreArrest should change to correct status and publish event`() {
        // GIVEN
        val publisher = mockk<DomainEventPublisher>(relaxed = true)
        val visionId = VisionId()
        val perpetratorId = PerpetratorId()
        val preArrest = PreArrest(visionId = visionId, perpetratorId = perpetratorId)
        preArrest.injectPublisher(publisher)

        // WHEN
        preArrest.executePreArrest()

        // THEN
        assertThat(preArrest.status).isEqualTo(PreArrestStatus.ARRESTED_BEFORE_CRIME)
        assertThat(preArrest.visionId).isEqualTo(visionId)
        assertThat(preArrest.perpetratorId).isEqualTo(perpetratorId)
        assertThat { preArrest.preArrestDate }.isNotNull()

        verify {
            publisher.publish(
                match<PreArrestExecutedEvent> {
                    it.visionId == visionId && it.perpetratorId == perpetratorId && it.preArrestId == preArrest.id
                }
            )
        }
    }
}
