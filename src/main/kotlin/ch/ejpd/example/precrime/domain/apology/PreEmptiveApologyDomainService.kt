package ch.ejpd.example.precrime.domain.apology

import ch.ejpd.example.precrime.domain.DomainEventPublisher
import ch.ejpd.example.precrime.domain.vision.CrimeType
import ch.ejpd.example.precrime.domain.vision.Vision
import org.jmolecules.ddd.annotation.Service

@Service
class PreEmptiveApologyDomainService(
    private val preApologyLetterService: PreApologyLetterService,
    private val publisher: DomainEventPublisher
) {

    fun generateApology(vision: Vision): PreApology {
        // Base compensation scales with crime severity
        val baseAmount = when (vision.crimeType) {
            CrimeType.MURDER -> 10000.0
            CrimeType.GRAND_THEFT_AUTO -> 5000.0
            CrimeType.JAYWALKING -> 50.0
            else -> 1000.0
        }

        // Dystopian recovery fees
        val jetpackFuelDeduction = 450.0 // Standard deployment fee
        val haloRentalFee = 250.0       // Stasis halo overnight fee
        val netPayout = baseAmount - jetpackFuelDeduction - haloRentalFee

        val compensation = Compensation(baseAmount, jetpackFuelDeduction, haloRentalFee, netPayout)
        val letterText = preApologyLetterService.generateLetterText(vision, compensation)

        val apology = PreApology(
            visionId = vision.id,
            perpetratorId = vision.perpetratorId,
            compensation = compensation,
            apologyLetter = ApologyLetter(letterText),
            publisher = publisher
        )
        apology.issue()
        return apology
    }
}
