package ch.ejpd.example.precrime.domain.enforcement

import ch.ejpd.example.precrime.domain.DomainEventPublisher
import org.jmolecules.ddd.annotation.AggregateRoot
import org.jmolecules.ddd.annotation.Identity
import org.jmolecules.ddd.types.Identifier
import org.jmolecules.event.annotation.DomainEvent
import java.util.*

@AggregateRoot
class LawEnforcementUnit(
    @Identity val id: EnforcementUnitId = EnforcementUnitId(UUID.randomUUID()),
    val unitName: String = "Pre-Crime Response Team Alpha"
) {
    private val activeArrests = mutableSetOf<PreArrest>()
    private var publisher: DomainEventPublisher? = null

    fun register(publisher: DomainEventPublisher) {
        this.publisher = publisher
    }

    fun executePreArrest(visionId: UUID, perpetrator: String): PreArrestExecuted {
        activeArrests.add(PreArrest(UUID.randomUUID(), visionId, perpetrator, "ARRESTED_BEFORE_CRIME"))
        val event = PreArrestExecuted(visionId, perpetrator)
        publisher?.publish(event)
        return event
    }
}

data class EnforcementUnitId(val value: UUID) : Identifier

data class PreArrest(
    val id: UUID,
    val visionId: UUID,
    val perpetrator: String,
    val status: String
)

@DomainEvent
data class PreArrestExecuted(
    val visionId: UUID,
    val perpetrator: String
)
