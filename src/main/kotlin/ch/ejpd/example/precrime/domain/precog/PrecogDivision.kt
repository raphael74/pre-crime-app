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
    @Identity val id: PrecogDivisionId = PrecogDivisionId(UUID.randomUUID()),
    var totalCrimesPrevented: Int = 0
) {
    private var publisher: DomainEventPublisher? = null

    fun register(publisher: DomainEventPublisher) {
        this.publisher = publisher
    }

    fun foreseeCrime(perpetrator: String, crimeType: String): CrimeForeseen {
        val event = CrimeForeseen(
            visionId = UUID.randomUUID(),
            perpetrator = perpetrator,
            crimeType = crimeType,
            foreseenAt = LocalDateTime.now().plusHours(2)
        )
        publisher?.publish(event)
        return event
    }

    fun recordPrevention() {
        this.totalCrimesPrevented++
    }
}

data class PrecogDivisionId(val value: UUID) : Identifier

@DomainEvent
data class CrimeForeseen(
    val visionId: UUID,
    val perpetrator: String,
    val crimeType: String,
    val foreseenAt: LocalDateTime
)
