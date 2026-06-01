package ch.ejpd.example.precrime.domain.apology

import ch.ejpd.example.precrime.domain.perpetrator.PerpetratorId
import ch.ejpd.example.precrime.domain.vision.CrimeType
import ch.ejpd.example.precrime.domain.vision.Vision
import ch.ejpd.example.precrime.domain.vision.VisionId
import ch.ejpd.example.precrime.infrastructure.integration.template.ThymeleafPreApologyLetterService
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class PreEmptiveApologyDomainServiceTest {

    private val letterService = mockk<ThymeleafPreApologyLetterService>()
    private val service = PreEmptiveApologyDomainService(letterService)

    @Test
    fun `should calculate positive net compensation for a high-severity crime (murder)`() {
        // GIVEN
        val perpetratorId = PerpetratorId()
        val vision = Vision(
            id = VisionId(),
            perpetratorId = perpetratorId,
            crimeType = CrimeType.MURDER,
            foreseenAt = LocalDateTime.now()
        )
        every { letterService.generateLetterText(any(), any()) } returns "Dummy letter"

        // WHEN
        val apology = service.generateApology(vision)

        // THEN
        assertThat(apology.perpetratorId).isEqualTo(perpetratorId)
        assertThat(apology.compensation.baseAmount).isEqualTo(10000.0)
        assertThat(apology.compensation.jetpackFuelDeduction).isEqualTo(450.0)
        assertThat(apology.compensation.haloRentalFee).isEqualTo(250.0)
        assertThat(apology.compensation.netPayout).isEqualTo(9300.0)
        assertThat(apology.compensation.isBillableToFamily()).isFalse()
    }

    @Test
    fun `should calculate negative net compensation for a low-severity crime (jaywalking)`() {
        // GIVEN
        val perpetratorId = PerpetratorId()
        val vision = Vision(
            id = VisionId(),
            perpetratorId = perpetratorId,
            crimeType = CrimeType.JAYWALKING,
            foreseenAt = LocalDateTime.now()
        )
        every { letterService.generateLetterText(any(), any()) } returns "Dummy letter"

        // WHEN
        val apology = service.generateApology(vision)

        // THEN
        assertThat(apology.perpetratorId).isEqualTo(perpetratorId)
        assertThat(apology.compensation.baseAmount).isEqualTo(50.0)
        assertThat(apology.compensation.jetpackFuelDeduction).isEqualTo(450.0)
        assertThat(apology.compensation.haloRentalFee).isEqualTo(250.0)
        assertThat(apology.compensation.netPayout).isEqualTo(-650.0)
        assertThat(apology.compensation.isBillableToFamily()).isTrue()
    }

    @Test
    fun `should calculate correct compensation for an unmapped default crime`() {
        // GIVEN
        val vision = Vision(
            id = VisionId(),
            perpetratorId = PerpetratorId(),
            crimeType = CrimeType.LARCENY,
            foreseenAt = LocalDateTime.now()
        )
        every { letterService.generateLetterText(any(), any()) } returns "Dummy letter"


        // WHEN
        val apology = service.generateApology(vision)

        // THEN
        assertThat(apology.compensation.baseAmount).isEqualTo(1000.0)
        assertThat(apology.compensation.netPayout).isEqualTo(300.0)
        assertThat(apology.compensation.isBillableToFamily()).isFalse()
    }
}
