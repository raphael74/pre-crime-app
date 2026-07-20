package org.example.precrime.infrastructure.integration.template

import org.example.precrime.domain.perpetrator.Perpetrator
import org.example.precrime.domain.perpetrator.PerpetratorId
import org.example.precrime.domain.perpetrator.PerpetratorRepository
import org.example.precrime.domain.preapology.Compensation
import org.example.precrime.domain.prearrest.PreArrest
import org.example.precrime.domain.prearrest.PreArrestId
import org.example.precrime.domain.prearrest.PreArrestRepository
import org.example.precrime.domain.vision.CrimeType
import org.example.precrime.domain.vision.Vision
import org.example.precrime.domain.vision.VisionId
import org.example.precrime.domain.vision.VisionRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.thymeleaf.context.Context
import org.thymeleaf.spring6.SpringTemplateEngine
import java.time.OffsetDateTime

class ThymeleafPreApologyLetterServiceTest {

    private val springTemplateEngine = mockk<SpringTemplateEngine>()
    private val preArrestRepository = mockk<PreArrestRepository>()
    private val visionRepository = mockk<VisionRepository>()
    private val perpetratorRepository = mockk<PerpetratorRepository>()

    private val service = ThymeleafPreApologyLetterService(
        springTemplateEngine = springTemplateEngine,
        preArrestRepository = preArrestRepository,
        visionRepository = visionRepository,
        perpetratorRepository = perpetratorRepository
    )

    @Test
    fun `should generate letter text with correct context variables`() {
        val preArrestId = PreArrestId()
        val perpetratorId = PerpetratorId()
        val visionId = VisionId()
        val perpetrator = Perpetrator(
            id = perpetratorId,
            firstName = "John",
            lastName = "Doe"
        )
        val vision = Vision(
            id = visionId,
            perpetratorId = perpetratorId,
            crimeType = CrimeType.MURDER,
            foreseenAt = OffsetDateTime.parse("2026-06-15T14:30:00+00:00")
        )
        val preArrest = PreArrest(
            id = preArrestId,
            visionId = visionId,
            perpetratorId = perpetratorId
        )
        val compensation = Compensation(
            baseAmount = 10000.0,
            jetpackFuelDeduction = 450.0,
            haloRentalFee = 250.0,
            netPayout = 9300.0
        )

        every { preArrestRepository.findById(preArrestId) } returns preArrest
        every { perpetratorRepository.findById(perpetratorId) } returns perpetrator
        every { visionRepository.findById(visionId) } returns vision

        val contextSlot = slot<Context>()
        every { springTemplateEngine.process("apology-letter-en.txt", capture(contextSlot)) } returns "Generated letter"

        val result = service.generateLetterText(preArrestId, compensation)

        assertThat(result).isEqualTo("Generated letter")
        verify { preArrestRepository.findById(preArrestId) }
        verify { perpetratorRepository.findById(perpetratorId) }
        verify { visionRepository.findById(visionId) }
        with(contextSlot.captured) {
            assertThat(getVariable("perpetratorName")).isEqualTo("John Doe")
            assertThat(getVariable("crimeType")).isEqualTo("Murder")
            assertThat(getVariable("jetpackFuelDeduction")).isEqualTo(450.0)
            assertThat(getVariable("haloRentalFee")).isEqualTo(250.0)
            assertThat(getVariable("netPayout")).isEqualTo(9300.0)
            assertThat(getVariable("foreseenAt")).isEqualTo("15.06.2026 14:30:00")
        }
    }

    @Test
    fun `should throw when PreArrest not found`() {
        val preArrestId = PreArrestId()
        val compensation = Compensation(1000.0, 450.0, 250.0, 300.0)

        every { preArrestRepository.findById(preArrestId) } returns null

        assertThatThrownBy { service.generateLetterText(preArrestId, compensation) }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining(preArrestId.toString())
    }

    @Test
    fun `should throw when Perpetrator not found`() {
        val preArrestId = PreArrestId()
        val perpetratorId = PerpetratorId()
        val visionId = VisionId()
        val preArrest = PreArrest(
            id = preArrestId,
            visionId = visionId,
            perpetratorId = perpetratorId
        )
        val compensation = Compensation(1000.0, 450.0, 250.0, 300.0)

        every { preArrestRepository.findById(preArrestId) } returns preArrest
        every { perpetratorRepository.findById(perpetratorId) } returns null

        assertThatThrownBy { service.generateLetterText(preArrestId, compensation) }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining(perpetratorId.toString())
    }

    @Test
    fun `should throw when Vision not found`() {
        val preArrestId = PreArrestId()
        val perpetratorId = PerpetratorId()
        val visionId = VisionId()
        val perpetrator = Perpetrator(id = perpetratorId, firstName = "Jane", lastName = "Smith")
        val preArrest = PreArrest(
            id = preArrestId,
            visionId = visionId,
            perpetratorId = perpetratorId
        )
        val compensation = Compensation(1000.0, 450.0, 250.0, 300.0)

        every { preArrestRepository.findById(preArrestId) } returns preArrest
        every { perpetratorRepository.findById(perpetratorId) } returns perpetrator
        every { visionRepository.findById(visionId) } returns null

        assertThatThrownBy { service.generateLetterText(preArrestId, compensation) }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining(visionId.toString())
    }

    @Test
    fun `should format date according to pattern dd_MM_yyyy HH_mm_ss`() {
        val preArrestId = PreArrestId()
        val perpetratorId = PerpetratorId()
        val visionId = VisionId()
        val perpetrator = Perpetrator(id = perpetratorId, firstName = "Alice", lastName = "Wonder")
        val vision = Vision(
            id = visionId,
            perpetratorId = perpetratorId,
            crimeType = CrimeType.JAYWALKING,
            foreseenAt = OffsetDateTime.parse("2026-01-01T09:05:03+00:00")
        )
        val preArrest = PreArrest(
            id = preArrestId,
            visionId = visionId,
            perpetratorId = perpetratorId
        )
        val compensation = Compensation(50.0, 450.0, 250.0, -650.0)

        every { preArrestRepository.findById(preArrestId) } returns preArrest
        every { perpetratorRepository.findById(perpetratorId) } returns perpetrator
        every { visionRepository.findById(visionId) } returns vision

        val contextSlot = slot<Context>()
        every { springTemplateEngine.process("apology-letter-en.txt", capture(contextSlot)) } returns "Letter"

        service.generateLetterText(preArrestId, compensation)

        assertThat(contextSlot.captured.getVariable("foreseenAt")).isEqualTo("01.01.2026 09:05:03")
    }
}
