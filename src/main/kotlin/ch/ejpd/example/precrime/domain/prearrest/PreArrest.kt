package ch.ejpd.example.precrime.domain.prearrest

import ch.ejpd.example.precrime.domain.AggregateVersion
import ch.ejpd.example.precrime.domain.DomainEventPublisher
import ch.ejpd.example.precrime.domain.perpetrator.PerpetratorId
import ch.ejpd.example.precrime.domain.vision.VisionId
import org.jmolecules.ddd.annotation.AggregateRoot
import org.jmolecules.ddd.annotation.Identity
import org.jmolecules.ddd.annotation.Repository
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
    var status: PreArrestStatus = PreArrestStatus.PENDING,
    private val publisher: DomainEventPublisher
) {
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
