package ch.ejpd.example.precrime.infrastructure.integration.template

import ch.ejpd.example.precrime.domain.perpetrator.PerpetratorRepository
import ch.ejpd.example.precrime.domain.preapology.Compensation
import ch.ejpd.example.precrime.domain.preapology.PreApologyLetterService
import ch.ejpd.example.precrime.domain.prearrest.PreArrestId
import ch.ejpd.example.precrime.domain.prearrest.PreArrestRepository
import ch.ejpd.example.precrime.domain.vision.VisionRepository
import org.springframework.stereotype.Service
import org.thymeleaf.context.Context
import org.thymeleaf.spring6.SpringTemplateEngine
import java.time.format.DateTimeFormatter

@Service
class ThymeleafPreApologyLetterService(
    private val springTemplateEngine: SpringTemplateEngine,
    private val preArrestRepository: PreArrestRepository,
    private val visionRepository: VisionRepository,
    private val perpetratorRepository: PerpetratorRepository
) : PreApologyLetterService {

    override fun generateLetterText(preArrestId: PreArrestId, compensation: Compensation): String {
        val preArrest = preArrestRepository.findById(preArrestId)
            ?: throw IllegalStateException("PreArrest $preArrestId not found")
        val perpetrator = perpetratorRepository.findById(preArrest.perpetratorId)
            ?: throw IllegalStateException("Perpetrator ${preArrest.perpetratorId} not found")
        val vision = visionRepository.findById(preArrest.visionId)
            ?: throw IllegalStateException("Vision ${preArrest.visionId} not found")

        val dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyyy HH:mm:ss")
        val context = Context().apply {
            setVariable("perpetratorName", perpetrator.fullName)
            setVariable("foreseenAt", vision.foreseenAt.format(dateTimeFormatter))
            setVariable("crimeType", vision.crimeType.value)
            setVariable("jetpackFuelDeduction", compensation.jetpackFuelDeduction)
            setVariable("haloRentalFee", compensation.haloRentalFee)
            setVariable("netPayout", compensation.netPayout)
        }
        return springTemplateEngine.process("apology-letter-en.txt", context)
    }
}