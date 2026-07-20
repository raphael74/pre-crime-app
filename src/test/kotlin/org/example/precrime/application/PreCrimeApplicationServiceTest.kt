package org.example.precrime.application

import org.example.precrime.domain.DomainEventPublisher
import org.example.precrime.domain.perpetrator.Perpetrator
import org.example.precrime.domain.perpetrator.PerpetratorRepository
import org.example.precrime.domain.preapology.*
import org.example.precrime.domain.prearrest.*
import org.example.precrime.domain.statistic.Statistic
import org.example.precrime.domain.statistic.StatisticRepository
import org.example.precrime.domain.vision.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

class PreCrimeApplicationServiceTest {

    private val visionRepository = mockk<VisionRepository>()
    private val statisticRepository = mockk<StatisticRepository>()
    private val preArrestRepository = mockk<PreArrestRepository>()
    private val preApologyRepository = mockk<PreApologyRepository>(relaxed = true)
    private val perpetratorRepository = mockk<PerpetratorRepository>()
    private val preApologyDomainService = mockk<PreApologyDomainService>()
    private val publisher = mockk<DomainEventPublisher>(relaxed = true)
    private val service = PreCrimeApplicationService(
        visionRepository,
        statisticRepository,
        preArrestRepository,
        preApologyRepository,
        perpetratorRepository,
        preApologyDomainService,
        publisher
    )

    @Test
    fun `getPreventedCrimesCount should return count from singleton statistic`() {
        // GIVEN
        val statistic = mockk<Statistic>()
        every { statistic.totalCrimesPrevented } returns 42
        every { statisticRepository.findSingleton() } returns statistic

        // WHEN
        val count = service.getPreventedCrimesCount()

        // THEN
        assertThat(count).isEqualTo(42)
        verify { statisticRepository.findSingleton() }
    }


    @Test
    fun `triggerVision should find singleton, foresee crime and save`() {
        // GIVEN
        val perpetrator = Perpetrator(firstName = "John", lastName = "Doe")
        every { perpetratorRepository.findByFirstAndLastName("John", "Doe") } returns perpetrator

        every { visionRepository.create(any()) } returns Unit
        val cmd = CreateVisionCommand("John", "Doe", CrimeType.MURDER)

        // WHEN
        service.triggerVision(cmd)

        // THEN
        verify {
            perpetratorRepository.findByFirstAndLastName("John", "Doe")
            visionRepository.create(any())
            publisher.publish(match { it is CrimeForeseenEvent })
        }
    }

    @Test
    fun `onCrimeForeseen should save pre-arrest`() {
        // GIVEN
        val visionId = VisionId()
        val perpetrator = Perpetrator(firstName = "John", lastName = "Doe")
        val event = CrimeForeseenEvent(visionId, perpetrator.id, CrimeType.MURDER, OffsetDateTime.now())
        every { preArrestRepository.create(any()) } returns Unit

        // WHEN
        service.onCrimeForeseen(event)

        // THEN
        verify {
            preArrestRepository.create(match {
                it.visionId == visionId && it.perpetratorId == perpetrator.id
            })
        }
    }

    @Test
    fun `onPreArrestExecuted should find singleton statistic, record prevention and save`() {
        // GIVEN
        val visionId = VisionId()
        val perpetrator = Perpetrator(firstName = "John", lastName = "Doe")
        val preArrestId = PreArrestId()
        val event = PreArrestExecutedEvent(preArrestId, visionId, perpetrator.id)

        val vision = Vision(
            id = visionId,
            perpetratorId = perpetrator.id,
            crimeType = CrimeType.MURDER,
            foreseenAt = OffsetDateTime.now()
        )
        every { visionRepository.findById(any()) } returns vision

        val statistic = mockk<Statistic>(relaxed = true)
        every { statisticRepository.findSingleton() } returns statistic
        every { statisticRepository.update(any()) } returns Unit

        val apology = PreApology(
            preArrestId = preArrestId,
            perpetratorId = perpetrator.id,
            compensation = Compensation(10000.0, 450.0, 250.0, 9300.0),
            apologyLetter = ApologyLetter("Dear family...")
        )
        every {
            preApologyDomainService.generatePreApology(
                preArrestId,
                perpetrator.id,
                vision.crimeType
            )
        } returns apology
        every { preApologyRepository.create(any()) } returns Unit

        // WHEN
        service.onPreArrestExecuted(event)

        // THEN
        verify {
            statisticRepository.findSingleton()
            statistic.recordPrevention()
            statisticRepository.update(statistic)
            preApologyDomainService.generatePreApology(preArrestId, perpetrator.id, vision.crimeType)
            preApologyRepository.create(apology)
            publisher.publish(match { it is PreApologyIssuedEvent })
        }
    }

    @Test
    fun `executePreArrest should execute and publish PreArrestExecutedEvent`() {
        // GIVEN
        val preArrestId = PreArrestId()
        val perpetrator = Perpetrator(firstName = "John", lastName = "Doe")
        val visionId = VisionId()
        val preArrest = PreArrest(id = preArrestId, visionId = visionId, perpetratorId = perpetrator.id)
        every { preArrestRepository.findById(preArrestId) } returns preArrest
        every { preArrestRepository.update(any()) } returns Unit

        // WHEN
        service.executePreArrest(preArrestId)

        // THEN
        verify {
            preArrestRepository.findById(preArrestId)
            preArrestRepository.update(preArrest)
            publisher.publish(match { it is PreArrestExecutedEvent && it.preArrestId == preArrestId })
        }
        assertThat(preArrest.status).isEqualTo(PreArrestStatus.ARRESTED_BEFORE_CRIME)
    }

    @Test
    fun `executePreArrest should throw when pre-arrest not found`() {
        // GIVEN
        val preArrestId = PreArrestId()
        every { preArrestRepository.findById(preArrestId) } returns null

        // WHEN & THEN
        assertThatThrownBy { service.executePreArrest(preArrestId) }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining(preArrestId.toString())
    }

    @Test
    fun `cancelPreArrest should cancel and publish PreArrestCancelledEvent`() {
        // GIVEN
        val preArrestId = PreArrestId()
        val perpetrator = Perpetrator(firstName = "John", lastName = "Doe")
        val visionId = VisionId()
        val preArrest = PreArrest(id = preArrestId, visionId = visionId, perpetratorId = perpetrator.id)
        every { preArrestRepository.findById(preArrestId) } returns preArrest
        every { preArrestRepository.update(any()) } returns Unit

        // WHEN
        service.cancelPreArrest(preArrestId)

        // THEN
        verify {
            preArrestRepository.findById(preArrestId)
            preArrestRepository.update(preArrest)
            publisher.publish(match { it is PreArrestCancelledEvent && it.preArrestId == preArrestId })
        }
        assertThat(preArrest.status).isEqualTo(PreArrestStatus.CANCELLED)
    }

    @Test
    fun `cancelPreArrest should throw when pre-arrest not found`() {
        // GIVEN
        val preArrestId = PreArrestId()
        every { preArrestRepository.findById(preArrestId) } returns null

        // WHEN & THEN
        assertThatThrownBy { service.cancelPreArrest(preArrestId) }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining(preArrestId.toString())
    }

    @Test
    fun `getAllApologies should return all apologies with perpetrators`() {
        // GIVEN
        val perpetrator = Perpetrator(firstName = "John", lastName = "Doe")
        val preArrestId = PreArrestId()
        val apology = PreApology(
            preArrestId = preArrestId,
            perpetratorId = perpetrator.id,
            compensation = Compensation(10000.0, 450.0, 250.0, 9300.0),
            apologyLetter = ApologyLetter("Dear family...")
        )
        every { preApologyRepository.findAll() } returns listOf(apology)
        every { perpetratorRepository.findById(perpetrator.id) } returns perpetrator

        // WHEN
        val result = service.getAllApologies()

        // THEN
        assertThat(result).hasSize(1)
        assertThat(result[0].apology).isEqualTo(apology)
        assertThat(result[0].perpetrator).isEqualTo(perpetrator)
        verify {
            preApologyRepository.findAll()
            perpetratorRepository.findById(perpetrator.id)
        }
    }

    @Test
    fun `getAllPendingPreArrests should return all pending pre-arrests with perpetrators`() {
        // GIVEN
        val perpetrator = Perpetrator(firstName = "John", lastName = "Doe")
        val visionId = VisionId()
        val preArrest = PreArrest(visionId = visionId, perpetratorId = perpetrator.id)
        every { preArrestRepository.findAllPending() } returns listOf(preArrest)
        every { perpetratorRepository.findById(perpetrator.id) } returns perpetrator

        // WHEN
        val result = service.getAllPendingPreArrests()

        // THEN
        assertThat(result).hasSize(1)
        assertThat(result[0].preArrest).isEqualTo(preArrest)
        assertThat(result[0].perpetrator).isEqualTo(perpetrator)
        verify {
            preArrestRepository.findAllPending()
            perpetratorRepository.findById(perpetrator.id)
        }
    }

    @Test
    fun `getAllExecutedPreArrests should return all executed pre-arrests with perpetrators`() {
        // GIVEN
        val perpetrator = Perpetrator(firstName = "John", lastName = "Doe")
        val visionId = VisionId()
        val preArrest = PreArrest(visionId = visionId, perpetratorId = perpetrator.id)
        every { preArrestRepository.findAllArrested() } returns listOf(preArrest)
        every { perpetratorRepository.findById(perpetrator.id) } returns perpetrator

        // WHEN
        val result = service.getAllExecutedPreArrests()

        // THEN
        assertThat(result).hasSize(1)
        assertThat(result[0].preArrest).isEqualTo(preArrest)
        assertThat(result[0].perpetrator).isEqualTo(perpetrator)
        verify {
            preArrestRepository.findAllArrested()
            perpetratorRepository.findById(perpetrator.id)
        }
    }
}
