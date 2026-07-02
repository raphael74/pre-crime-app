package ch.ejpd.example.precrime.domain.preapology

import ch.ejpd.example.precrime.domain.DomainEventPublisher
import ch.ejpd.example.precrime.domain.perpetrator.PerpetratorId
import ch.ejpd.example.precrime.domain.prearrest.PreArrestId
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class PreApologyTest {

    @Test
    fun `issue should publish PreApologyIssuedEvent`() {
        val publisher = mockk<DomainEventPublisher>(relaxed = true)
        val preApology = PreApology(
            preArrestId = PreArrestId(),
            perpetratorId = PerpetratorId(),
            compensation = Compensation(10000.0, 450.0, 250.0, 9300.0),
            apologyLetter = ApologyLetter("Dear family...")
        ).apply { injectPublisher(publisher) }

        preApology.issue()

        verify {
            publisher.publish(
                match<PreApologyIssuedEvent> {
                    it.apologyId == preApology.id &&
                            it.preArrestId == preApology.preArrestId &&
                            it.perpetratorId == preApology.perpetratorId &&
                            it.netPayout == preApology.compensation.netPayout
                }
            )
        }
    }

    @Test
    fun `issue should throw when publisher not injected`() {
        val preApology = PreApology(
            preArrestId = PreArrestId(),
            perpetratorId = PerpetratorId(),
            compensation = Compensation(10000.0, 450.0, 250.0, 9300.0),
            apologyLetter = ApologyLetter("Dear family...")
        )

        assertThatThrownBy { preApology.issue() }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("DomainEventPublisher has not been injected into PreApology")
    }

    @Test
    fun `compensation with negative netPayout should be billable`() {
        val compensation = Compensation(50.0, 450.0, 250.0, -650.0)

        assertThat(compensation.isBillableToFamily()).isTrue()
    }

    @Test
    fun `compensation with positive netPayout should not be billable`() {
        val compensation = Compensation(10000.0, 450.0, 250.0, 9300.0)

        assertThat(compensation.isBillableToFamily()).isFalse()
    }

    @Test
    fun `apology letter with blank text should throw`() {
        assertThatThrownBy { ApologyLetter("") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Letter text cannot be blank")

        assertThatThrownBy { ApologyLetter("  ") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Letter text cannot be blank")
    }

    @Test
    fun `apology letter with valid text should be created`() {
        val letter = ApologyLetter("We regret to inform you...")
        assertThat(letter.text).isEqualTo("We regret to inform you...")
    }
}
