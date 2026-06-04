package ch.ejpd.example.precrime.domain.prearrest

import ch.ejpd.example.precrime.domain.AggregateVersion
import ch.ejpd.example.precrime.domain.DomainEventPublisher
import ch.ejpd.example.precrime.domain.perpetrator.PerpetratorId
import ch.ejpd.example.precrime.domain.vision.VisionId
import org.jmolecules.ddd.annotation.AggregateRoot
import org.jmolecules.ddd.annotation.Identity
import org.jmolecules.ddd.annotation.Repository
import org.jmolecules.ddd.annotation.ValueObject
import org.jmolecules.ddd.types.Identifier
import org.jmolecules.event.annotation.DomainEvent
import java.time.OffsetDateTime
import java.util.*

@AggregateRoot
class PreArrest(
    @Identity val id: PreArrestId = PreArrestId(),
    var version: AggregateVersion = AggregateVersion(),
    val visionId: VisionId,
    val perpetratorId: PerpetratorId,
    val preArrestIssuedDate: OffsetDateTime = OffsetDateTime.now(),
    var preArrestDate: OffsetDateTime? = null,
    var status: PreArrestStatus = PreArrestStatus.PENDING
) {
    private var publisher: DomainEventPublisher? = null

    fun injectPublisher(publisher: DomainEventPublisher) {
        this.publisher = publisher
    }

    fun executePreArrest() {
        val pub = requireNotNull(publisher) { "DomainEventPublisher has not been injected into PreArrest $id" }
        status = PreArrestStatus.ARRESTED_BEFORE_CRIME
        preArrestDate = OffsetDateTime.now()
        pub.publish(PreArrestExecutedEvent(id, visionId, perpetratorId))
    }

    fun cancelPreArrest() {
        val pub = requireNotNull(publisher) { "DomainEventPublisher has not been injected into PreArrest $id" }
        status = PreArrestStatus.CANCELLED
        pub.publish(PreArrestCancelledEvent(id, visionId, perpetratorId))
    }
}

@ValueObject
enum class PreArrestStatus {
    PENDING,
    ARRESTED_BEFORE_CRIME,
    CANCELLED
}

@JvmInline
value class PreArrestId(val value: UUID = UUID.randomUUID()) : Identifier

@DomainEvent
data class PreArrestExecutedEvent(
    val preArrestId: PreArrestId,
    val visionId: VisionId,
    val perpetratorId: PerpetratorId
)

@DomainEvent
data class PreArrestCancelledEvent(
    val preArrestId: PreArrestId,
    val visionId: VisionId,
    val perpetratorId: PerpetratorId
)

@Repository
interface PreArrestRepository {
    fun findById(id: PreArrestId): PreArrest?
    fun save(preArrest: PreArrest)
    fun findAll(): List<PreArrest>
    fun findAllArrested(): List<PreArrest>
    fun findAllPending(): List<PreArrest>
}
