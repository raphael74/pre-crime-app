package ch.ejpd.example.precrime.domain.preapology

import ch.ejpd.example.precrime.domain.prearrest.PreArrestId

interface PreApologyLetterService {
    fun generateLetterText(preArrestId: PreArrestId, compensation: Compensation): String
}
