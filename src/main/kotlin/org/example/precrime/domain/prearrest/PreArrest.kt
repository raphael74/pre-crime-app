package org.example.precrime.domain.prearrest

import org.example.precrime.domain.AggregateVersion
import org.example.precrime.domain.DomainEventPublisher
import org.example.precrime.domain.perpetrator.PerpetratorId
import org.example.precrime.domain.vision.VisionId
import org.jmolecules.ddd.annotation.AggregateRoot
import org.jmolecules.ddd.annotation.Identity
import org.jmolecules.ddd.annotation.ValueObject
import org.jmolecules.ddd.types.Identifier
import org.jmolecules.event.annotation.DomainEvent
import java.time.OffsetDateTime
import java.util.UUID

@AggregateRoot
class PreArrest(
    @Identity val id: PreArrestId = PreArrestId(),
    var version: AggregateVersion = AggregateVersion(),
    val visionId: VisionId,
    val perpetratorId: PerpetratorId,
    val preArrestIssuedDate: OffsetDateTime = OffsetDateTime.now(),
    var preArrestDate: OffsetDateTime? = null,
    var status: PreArrestStatus = PreArrestStatus.PENDING,
) {
    private var eventPublisher: DomainEventPublisher? = null

    fun injectPublisher(publisher: DomainEventPublisher) {
        this.eventPublisher = publisher
    }

    private val publisher: DomainEventPublisher
        get() =
            requireNotNull(eventPublisher) {
                "DomainEventPublisher has not been injected into PreArrest $id"
            }

    fun executePreArrest() {
        status = PreArrestStatus.ARRESTED_BEFORE_CRIME
        preArrestDate = OffsetDateTime.now()
        publisher.publish(PreArrestExecutedEvent(id, visionId, perpetratorId))
    }

    fun cancelPreArrest() {
        status = PreArrestStatus.CANCELLED
        publisher.publish(PreArrestCancelledEvent(id, visionId, perpetratorId))
    }
}

@ValueObject
enum class PreArrestStatus {
    PENDING,
    ARRESTED_BEFORE_CRIME,
    CANCELLED,
}

@JvmInline
value class PreArrestId(
    val value: UUID = UUID.randomUUID(),
) : Identifier

@DomainEvent
data class PreArrestExecutedEvent(
    val preArrestId: PreArrestId,
    val visionId: VisionId,
    val perpetratorId: PerpetratorId,
)

@DomainEvent
data class PreArrestCancelledEvent(
    val preArrestId: PreArrestId,
    val visionId: VisionId,
    val perpetratorId: PerpetratorId,
)
