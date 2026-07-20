package org.example.precrime.domain.preapology

import org.example.precrime.domain.perpetrator.PerpetratorId
import org.example.precrime.domain.prearrest.PreArrestId
import org.example.precrime.domain.vision.CrimeType
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
