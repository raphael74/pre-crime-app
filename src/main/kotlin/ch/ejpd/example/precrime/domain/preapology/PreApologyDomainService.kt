package ch.ejpd.example.precrime.domain.preapology

import ch.ejpd.example.precrime.domain.perpetrator.PerpetratorId
import ch.ejpd.example.precrime.domain.prearrest.PreArrestId
import ch.ejpd.example.precrime.domain.vision.CrimeType
import org.jmolecules.ddd.annotation.Service

@Service
class PreApologyDomainService(
    private val preApologyLetterService: PreApologyLetterService,
) {

    fun generatePreApology(preArrestId: PreArrestId, perpetratorId: PerpetratorId, crimeType: CrimeType): PreApology {
        // Base compensation scales with crime severity
        val baseAmount = when (crimeType) {
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
        val letterText = preApologyLetterService.generateLetterText(preArrestId, compensation)

        return PreApology(
            preArrestId = preArrestId,
            perpetratorId = perpetratorId,
            compensation = compensation,
            apologyLetter = ApologyLetter(letterText)
        )
    }
}
