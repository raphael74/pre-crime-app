package ch.ejpd.example.precrime.domain.enforcement

import ch.ejpd.example.precrime.domain.AggregateVersion
import ch.ejpd.example.precrime.domain.precog.Perpetrator
import ch.ejpd.example.precrime.domain.precog.VisionId
import org.jmolecules.ddd.annotation.*
import org.jmolecules.ddd.types.Identifier
import org.jmolecules.event.annotation.DomainEvent
import java.util.*

@AggregateRoot
class LawEnforcementUnit(
    @Identity val id: EnforcementUnitId = EnforcementUnitId(),
    var version: AggregateVersion = AggregateVersion(),
    val unitName: UnitName = UnitName("Pre-Crime Response Team Alpha"),
    preArrests: Set<PreArrest> = emptySet()
) {
    private val _preArrests: MutableSet<PreArrest> = preArrests.toMutableSet()
    private val _events = mutableListOf<Any>()

    val preArrests: Set<PreArrest> get() = _preArrests.toSet()
    val domainEvents: List<Any> get() = _events.toList()

    fun clearDomainEvents() {
        _events.clear()
    }

    fun executePreArrest(visionId: VisionId, perpetrator: Perpetrator) {
        val preArrestId = PreArrestId()
        _preArrests.add(PreArrest(preArrestId, visionId, perpetrator, PreArrestStatus.ARRESTED_BEFORE_CRIME))
        _events.add(PreArrestExecutedEvent(preArrestId, visionId, perpetrator))
    }
}

@ValueObject
data class UnitName(val value: String) {
    init {
        require(value.isNotBlank()) { "Unit name cannot be blank" }
    }
}

enum class PreArrestStatus {
    ARRESTED_BEFORE_CRIME,
    RELEASED,
    ERRONEOUS_VISION
}

@JvmInline
value class EnforcementUnitId(val value: UUID = UUID.randomUUID()) : Identifier

@JvmInline
value class PreArrestId(val value: UUID = UUID.randomUUID()) : Identifier

@Entity
data class PreArrest(
    @Identity val id: PreArrestId,
    val visionId: VisionId,
    val perpetrator: Perpetrator,
    val status: PreArrestStatus
)

@DomainEvent
data class PreArrestExecutedEvent(
    val preArrestId: PreArrestId,
    val visionId: VisionId,
    val perpetrator: Perpetrator
)

@Repository
interface LawEnforcementRepository {
    fun findById(id: EnforcementUnitId): LawEnforcementUnit?
    fun update(unit: LawEnforcementUnit)
    fun findSingleton(): LawEnforcementUnit
}
