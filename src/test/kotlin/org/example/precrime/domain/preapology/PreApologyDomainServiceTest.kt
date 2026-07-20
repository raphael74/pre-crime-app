package org.example.precrime.domain.preapology

import org.example.precrime.domain.perpetrator.PerpetratorId
import org.example.precrime.domain.prearrest.PreArrestId
import org.example.precrime.domain.vision.CrimeType
import org.example.precrime.domain.vision.Vision
import org.example.precrime.domain.vision.VisionId
import org.example.precrime.infrastructure.integration.template.ThymeleafPreApologyLetterService
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

class PreApologyDomainServiceTest {

    private val letterService = mockk<ThymeleafPreApologyLetterService>()
    private val service = PreApologyDomainService(letterService)

    @Test
    fun `should calculate positive net compensation for a high-severity crime (murder)`() {
        // GIVEN
        val perpetratorId = PerpetratorId()
        val preArrestId = PreArrestId()
        val vision = Vision(
            id = VisionId(),
            perpetratorId = perpetratorId,
            crimeType = CrimeType.MURDER,
            foreseenAt = OffsetDateTime.now()
        )
        every { letterService.generateLetterText(any(), any()) } returns "Dummy letter"

        // WHEN
        val apology = service.generatePreApology(preArrestId, perpetratorId, vision.crimeType)

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
        val preArrestId = PreArrestId()
        val vision = Vision(
            id = VisionId(),
            perpetratorId = perpetratorId,
            crimeType = CrimeType.JAYWALKING,
            foreseenAt = OffsetDateTime.now()
        )
        every { letterService.generateLetterText(any(), any()) } returns "Dummy letter"

        // WHEN
        val apology = service.generatePreApology(preArrestId, perpetratorId, vision.crimeType)

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
        val perpetratorId = PerpetratorId()
        val preArrestId = PreArrestId()
        val vision = Vision(
            id = VisionId(),
            perpetratorId = perpetratorId,
            crimeType = CrimeType.LARCENY,
            foreseenAt = OffsetDateTime.now()
        )
        every { letterService.generateLetterText(any(), any()) } returns "Dummy letter"

        // WHEN
        val apology = service.generatePreApology(preArrestId, perpetratorId, vision.crimeType)

        // THEN
        assertThat(apology.compensation.baseAmount).isEqualTo(1000.0)
        assertThat(apology.compensation.netPayout).isEqualTo(300.0)
        assertThat(apology.compensation.isBillableToFamily()).isFalse()
    }
}
