package ch.ejpd.example.precrime.domain.apology

import ch.ejpd.example.precrime.domain.precog.Vision
import org.jmolecules.ddd.annotation.Service

@Service
class PreEmptiveApologyService {

    fun generateApology(vision: Vision): PreApology {
        // Base compensation scales with crime severity
        val baseAmount = when (vision.crimeType.value.lowercase()) {
            "murder" -> 10000.0
            "grand theft auto" -> 5000.0
            "jaywalking" -> 50.0
            else -> 1000.0
        }

        // Dystopian recovery fees
        val jetpackFuelDeduction = 450.0 // Standard deployment fee
        val haloRentalFee = 250.0       // Stasis halo overnight fee
        val netPayout = baseAmount - jetpackFuelDeduction - haloRentalFee

        val letterText = """
            Dear Family of ${vision.perpetrator.name},
            
            We are writing to formally apologize for the pre-arrest of your relative on ${vision.foreseenAt}.
            Our precogs foresaw them committing the crime of "${vision.crimeType.value}". 
            
            Under Section 42 of the Pre-Crime Act, we have processed a goodwill compensation package.
            To offset deployment costs, we have deducted a Jetpack Fuel Recovery Fee ($$jetpackFuelDeduction) and a Stasis Halo Rental Surcharge ($$haloRentalFee).
            
            ${
            if (netPayout >= 0) {
                "A net balance of $$netPayout has been credited to your Future-Wallet."
            } else {
                "Please remit the remaining balance of $${-netPayout} to the Pre-Crime Department within 30 days."
            }
        }
            
            Have a pleasant, crime-free day!
            - Department of Pre-Crime Bureaucracy
        """.trimIndent()

        val apology = PreApology(
            visionId = vision.id,
            perpetrator = vision.perpetrator,
            compensation = Compensation(baseAmount, jetpackFuelDeduction, haloRentalFee, netPayout),
            apologyLetter = ApologyLetter(letterText)
        )
        apology.issue()
        return apology
    }
}
