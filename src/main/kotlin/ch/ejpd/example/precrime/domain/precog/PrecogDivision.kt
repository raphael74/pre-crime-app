package ch.ejpd.example.precrime.domain.precog

import ch.ejpd.example.precrime.domain.AggregateVersion
import org.jmolecules.ddd.annotation.*
import org.jmolecules.ddd.types.Identifier
import org.jmolecules.event.annotation.DomainEvent
import java.time.LocalDateTime
import java.util.*

@AggregateRoot
class PrecogDivision(
    @Identity val id: PrecogDivisionId = PrecogDivisionId(),
    var version: AggregateVersion = AggregateVersion(),
    var totalCrimesPrevented: Int = 0,
    visions: Set<Vision> = emptySet()
) {
    private val _visions: MutableSet<Vision> = visions.toMutableSet()
    private val _events = mutableListOf<Any>()

    val visions: Set<Vision> get() = _visions.toSet()
    val domainEvents: List<Any> get() = _events.toList()

    fun clearDomainEvents() {
        _events.clear()
    }

    fun foreseeCrime(perpetrator: Perpetrator, crimeType: CrimeType): VisionId {
        val vision = VisionFactory.createVision(perpetrator, crimeType)
        _visions.add(vision)

        _events.add(
            CrimeForeseenEvent(
                visionId = vision.id,
                perpetrator = vision.perpetrator,
                crimeType = vision.crimeType,
                foreseenAt = vision.foreseenAt
            )
        )
        return vision.id
    }

    fun recordPrevention() {
        this.totalCrimesPrevented++
    }
}

@ValueObject
data class Perpetrator(val name: String) {
    init {
        require(name.isNotBlank()) { "Perpetrator name cannot be blank" }
    }
}

@ValueObject
data class CrimeType(val value: String) {
    init {
        require(value.isNotBlank()) { "Crime type cannot be blank" }
    }
}

@Entity
data class Vision(
    @Identity val id: VisionId,
    val perpetrator: Perpetrator,
    val crimeType: CrimeType,
    val foreseenAt: LocalDateTime
)

@Factory
class VisionFactory {
    companion object {
        fun createVision(perpetrator: Perpetrator, crimeType: CrimeType): Vision {
            val visionId = VisionId()
            val foreseenAt = LocalDateTime.now().plusHours(2)
            return Vision(visionId, perpetrator, crimeType, foreseenAt)
        }
    }
}

@JvmInline
value class PrecogDivisionId(val value: UUID = UUID.randomUUID()) : Identifier

@JvmInline
value class VisionId(val value: UUID = UUID.randomUUID()) : Identifier

@DomainEvent
data class CrimeForeseenEvent(
    val visionId: VisionId,
    val perpetrator: Perpetrator,
    val crimeType: CrimeType,
    val foreseenAt: LocalDateTime
)

@Repository
interface PrecogDivisionRepository {
    fun findById(id: PrecogDivisionId): PrecogDivision?
    fun save(division: PrecogDivision)
    fun findSingleton(): PrecogDivision
}
