package org.example.precrime.domain.preapology

import org.example.precrime.domain.prearrest.PreArrestId

interface PreApologyLetterService {
    fun generateLetterText(preArrestId: PreArrestId, compensation: Compensation): String
}
