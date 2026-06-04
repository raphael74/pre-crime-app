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

        val compensation = CompensationFactory.createCompensation(crimeType)

        val letterText = preApologyLetterService.generateLetterText(preArrestId, compensation)

        return PreApology(
            preArrestId = preArrestId,
            perpetratorId = perpetratorId,
            compensation = compensation,
            apologyLetter = ApologyLetter(letterText)
        )
    }
}
