package ch.ejpd.example.precrime.domain.precog

import ch.ejpd.example.precrime.domain.DomainEventPublisher
import org.jmolecules.ddd.annotation.*
import org.jmolecules.ddd.types.Identifier
import org.jmolecules.event.annotation.DomainEvent
import java.time.LocalDateTime
import java.util.*

@AggregateRoot
class PrecogDivision(
    @Identity val id: PrecogDivisionId = PrecogDivisionId(),
    var version: Long = 0,
    var totalCrimesPrevented: Int = 0,
    val visions: MutableSet<Vision> = mutableSetOf()
) {
    private var publisher: DomainEventPublisher? = null

    fun register(publisher: DomainEventPublisher) {
        this.publisher = publisher
    }

    fun foreseeCrime(perpetrator: String, crimeType: String): VisionId {

        val vision = VisionFactory.createVision(perpetrator, crimeType)
        visions.add(vision)

        val event = CrimeForeseenEvent(
            visionId = vision.id,
            perpetrator = vision.perpetrator,
            crimeType = vision.crimeType,
            foreseenAt = vision.foreseenAt
        )
        publisher?.publish(event)
        return vision.id
    }

    fun recordPrevention() {
        this.totalCrimesPrevented++
    }
}

@Entity
data class Vision(
    @Identity val id: VisionId,
    val perpetrator: String,
    val crimeType: String,
    val foreseenAt: LocalDateTime
)

@Factory
class VisionFactory {
    companion object {
        fun createVision(perpetrator: String, crimeType: String): Vision {
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
    val perpetrator: String,
    val crimeType: String,
    val foreseenAt: LocalDateTime
)

@Repository
interface PrecogDivisionRepository {
    fun findById(id: PrecogDivisionId): PrecogDivision?
    fun save(division: PrecogDivision)
    fun findSingleton(): PrecogDivision
}