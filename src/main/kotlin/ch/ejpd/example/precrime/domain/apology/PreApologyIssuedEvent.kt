package ch.ejpd.example.precrime.domain.apology

import ch.ejpd.example.precrime.domain.precog.Perpetrator
import ch.ejpd.example.precrime.domain.precog.VisionId
import org.jmolecules.event.annotation.DomainEvent

@DomainEvent
data class PreApologyIssuedEvent(
    val apologyId: PreApologyId,
    val visionId: VisionId,
    val perpetrator: Perpetrator,
    val netPayout: Double
)
