package ch.ejpd.example.precrime.infrastructure.integration.template

import ch.ejpd.example.precrime.domain.apology.Compensation
import ch.ejpd.example.precrime.domain.apology.PreApologyLetterService
import ch.ejpd.example.precrime.domain.precog.Vision
import org.springframework.stereotype.Service
import org.thymeleaf.context.Context
import org.thymeleaf.spring6.SpringTemplateEngine

@Service
class ThymeleafPreApologyLetterService(
    private val springTemplateEngine: SpringTemplateEngine
) : PreApologyLetterService {

    override fun generateLetterText(vision: Vision, compensation: Compensation): String {
        val context = Context().apply {
            setVariable("perpetratorName", vision.perpetrator.name)
            setVariable("foreseenAt", vision.foreseenAt)
            setVariable("crimeType", vision.crimeType.value)
            setVariable("jetpackFuelDeduction", compensation.jetpackFuelDeduction)
            setVariable("haloRentalFee", compensation.haloRentalFee)
            setVariable("netPayout", compensation.netPayout)
        }
        return springTemplateEngine.process("apology-letter-en.txt", context)
    }
}