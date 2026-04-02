package ch.ejpd.example.precrime.domain.precog

import ch.ejpd.example.precrime.domain.DomainEventPublisher
import org.jmolecules.ddd.annotation.AggregateRoot
import org.jmolecules.ddd.annotation.Identity
import org.jmolecules.ddd.types.Identifier
import org.jmolecules.event.annotation.DomainEvent
import java.time.LocalDateTime
import java.util.*

@AggregateRoot
class PrecogDivision(
    @Identity val id: PrecogDivisionId = PrecogDivisionId(),
    var totalCrimesPrevented: Int = 0
) {
    private var publisher: DomainEventPublisher? = null

    fun register(publisher: DomainEventPublisher) {
        this.publisher = publisher
    }

    fun foreseeCrime(perpetrator: String, crimeType: String): VisionId {
        val visionId = VisionId()
        val event = CrimeForeseenEvent(
            visionId = visionId,
            perpetrator = perpetrator,
            crimeType = crimeType,
            foreseenAt = LocalDateTime.now().plusHours(2)
        )
        publisher?.publish(event)
        return visionId
    }

    fun recordPrevention() {
        this.totalCrimesPrevented++
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
