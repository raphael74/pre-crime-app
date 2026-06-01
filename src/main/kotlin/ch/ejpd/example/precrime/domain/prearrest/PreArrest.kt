package ch.ejpd.example.precrime.domain.prearrest

import ch.ejpd.example.precrime.domain.AggregateVersion
import ch.ejpd.example.precrime.domain.perpetrator.PerpetratorId
import ch.ejpd.example.precrime.domain.vision.VisionId
import org.jmolecules.ddd.annotation.AggregateRoot
import org.jmolecules.ddd.annotation.Identity
import org.jmolecules.ddd.annotation.Repository
import org.jmolecules.ddd.types.Identifier
import org.jmolecules.event.annotation.DomainEvent
import java.util.*

@AggregateRoot
class PreArrest(
    @Identity val id: PreArrestId = PreArrestId(),
    var version: AggregateVersion = AggregateVersion(),
    val visionId: VisionId,
    val perpetratorId: PerpetratorId,
    val status: PreArrestStatus = PreArrestStatus.ARRESTED_BEFORE_CRIME
) {
    private val _events = mutableListOf<Any>()
    val domainEvents: List<Any> get() = _events.toList()

    fun clearDomainEvents() {
        _events.clear()
    }

    init {
        if (status == PreArrestStatus.ARRESTED_BEFORE_CRIME) {
            _events.add(PreArrestExecutedEvent(id, visionId, perpetratorId))
        }
    }
}

enum class PreArrestStatus {
    ARRESTED_BEFORE_CRIME,
    RELEASED,
    ERRONEOUS_VISION
}

@JvmInline
value class PreArrestId(val value: UUID = UUID.randomUUID()) : Identifier

@DomainEvent
data class PreArrestExecutedEvent(
    val preArrestId: PreArrestId,
    val visionId: VisionId,
    val perpetratorId: PerpetratorId
)

@Repository
interface PreArrestRepository {
    fun findById(id: PreArrestId): PreArrest?
    fun save(preArrest: PreArrest)
    fun findAll(): List<PreArrest>
}
