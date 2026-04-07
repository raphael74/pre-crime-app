package ch.ejpd.example.precrime.domain.enforcement

import ch.ejpd.example.precrime.domain.DomainEventPublisher
import ch.ejpd.example.precrime.domain.precog.VisionId
import org.jmolecules.ddd.annotation.AggregateRoot
import org.jmolecules.ddd.annotation.Identity
import org.jmolecules.ddd.types.Identifier
import org.jmolecules.event.annotation.DomainEvent
import java.util.*

@AggregateRoot
class LawEnforcementUnit(
    @Identity val id: EnforcementUnitId = EnforcementUnitId(),
    val unitName: String = "Pre-Crime Response Team Alpha",
    val preArrests: MutableSet<PreArrest> = mutableSetOf()
) {
    private var publisher: DomainEventPublisher? = null

    fun register(publisher: DomainEventPublisher) {
        this.publisher = publisher
    }

    fun executePreArrest(visionId: VisionId, perpetrator: String) {
        val preArrestId = PreArrestId()
        preArrests.add(PreArrest(preArrestId, visionId, perpetrator, "ARRESTED_BEFORE_CRIME"))
        val event = PreArrestExecutedEvent(preArrestId, visionId, perpetrator)
        publisher?.publish(event)
    }
}

@JvmInline
value class EnforcementUnitId(val value: UUID = UUID.randomUUID()) : Identifier

@JvmInline
value class PreArrestId(val value: UUID = UUID.randomUUID()) : Identifier

data class PreArrest(
    val id: PreArrestId,
    val visionId: VisionId,
    val perpetrator: String,
    val status: String
)

@DomainEvent
data class PreArrestExecutedEvent(
    val preArrestId: PreArrestId,
    val visionId: VisionId,
    val perpetrator: String
)
