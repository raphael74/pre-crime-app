package ch.ejpd.example.precrime.domain.apology

import ch.ejpd.example.precrime.domain.enforcement.LawEnforcementUnit
import ch.ejpd.example.precrime.domain.precog.CrimeType
import ch.ejpd.example.precrime.domain.precog.Perpetrator
import ch.ejpd.example.precrime.domain.precog.Vision
import ch.ejpd.example.precrime.domain.precog.VisionId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class PreEmptiveApologyServiceTest {

    private val service = PreEmptiveApologyService()
    private val enforcementUnit = LawEnforcementUnit()

    @Test
    fun `should calculate positive net compensation for a high-severity crime (murder)`() {
        // GIVEN
        val vision = Vision(
            id = VisionId(),
            perpetrator = Perpetrator("Danny Witwer"),
            crimeType = CrimeType("Murder"),
            foreseenAt = LocalDateTime.now()
        )

        // WHEN
        val apology = service.generateApology(vision)

        // THEN
        assertThat(apology.perpetrator.name).isEqualTo("Danny Witwer")
        assertThat(apology.compensation.baseAmount).isEqualTo(10000.0)
        assertThat(apology.compensation.jetpackFuelDeduction).isEqualTo(450.0)
        assertThat(apology.compensation.haloRentalFee).isEqualTo(250.0)
        assertThat(apology.compensation.netPayout).isEqualTo(9300.0)
        assertThat(apology.compensation.isBillableToFamily()).isFalse()
        assertThat(apology.apologyLetter.text).contains("A net balance of $9300.0 has been credited to your Future-Wallet.")
    }

    @Test
    fun `should calculate negative net compensation for a low-severity crime (jaywalking)`() {
        // GIVEN
        val vision = Vision(
            id = VisionId(),
            perpetrator = Perpetrator("Arthur Pendelton"),
            crimeType = CrimeType("Jaywalking"),
            foreseenAt = LocalDateTime.now()
        )

        // WHEN
        val apology = service.generateApology(vision)

        // THEN
        assertThat(apology.perpetrator.name).isEqualTo("Arthur Pendelton")
        assertThat(apology.compensation.baseAmount).isEqualTo(50.0)
        assertThat(apology.compensation.jetpackFuelDeduction).isEqualTo(450.0)
        assertThat(apology.compensation.haloRentalFee).isEqualTo(250.0)
        assertThat(apology.compensation.netPayout).isEqualTo(-650.0)
        assertThat(apology.compensation.isBillableToFamily()).isTrue()
        assertThat(apology.apologyLetter.text).contains("Please remit the remaining balance of $650.0 to the Pre-Crime Department within 30 days.")
    }

    @Test
    fun `should calculate correct compensation for an unmapped default crime`() {
        // GIVEN
        val vision = Vision(
            id = VisionId(),
            perpetrator = Perpetrator("John Doe"),
            crimeType = CrimeType("Larceny"),
            foreseenAt = LocalDateTime.now()
        )

        // WHEN
        val apology = service.generateApology(vision)

        // THEN
        assertThat(apology.compensation.baseAmount).isEqualTo(1000.0)
        assertThat(apology.compensation.netPayout).isEqualTo(300.0)
        assertThat(apology.compensation.isBillableToFamily()).isFalse()
    }
}
