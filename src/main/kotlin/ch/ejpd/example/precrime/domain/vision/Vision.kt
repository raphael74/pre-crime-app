package ch.ejpd.example.precrime.domain.vision

import ch.ejpd.example.precrime.domain.AggregateVersion
import ch.ejpd.example.precrime.domain.DomainEventPublisher
import ch.ejpd.example.precrime.domain.perpetrator.PerpetratorId
import org.jmolecules.ddd.annotation.*
import org.jmolecules.ddd.types.Identifier
import org.jmolecules.event.annotation.DomainEvent
import java.time.LocalDateTime
import java.util.*

@AggregateRoot
class Vision(
    @Identity val id: VisionId,
    var version: AggregateVersion = AggregateVersion(),
    val perpetratorId: PerpetratorId,
    val crimeType: CrimeType,
    val foreseenAt: LocalDateTime
) {
    private var publisher: DomainEventPublisher? = null

    fun injectPublisher(publisher: DomainEventPublisher) {
        this.publisher = publisher
    }

    fun foreseeCrime() {
        val pub = requireNotNull(publisher) { "DomainEventPublisher has not been injected into Vision $id" }
        pub.publish(
            CrimeForeseenEvent(
                visionId = id,
                perpetratorId = perpetratorId,
                crimeType = crimeType,
                foreseenAt = foreseenAt
            )
        )
    }
}

@ValueObject
enum class CrimeType(val value: String) {
    MURDER("Murder"),
    THEFT("Theft"),
    ASSAULT("Assault"),
    ROBBERY("Robbery"),
    BURGLARY("Burglary"),
    FRAUD("Fraud"),
    ARSON("Arson"),
    KIDNAPPING("Kidnapping"),
    LARCENY("Larceny"),
    JAYWALKING("Jaywalking"),
    VANDALISM("Vandalism"),
    GRAND_THEFT_AUTO("Grand Theft Auto"),
    IDENTITY_THEFT("Identity Theft"),
    TAX_EVASION("Tax Evasion");
}

@Factory
class VisionFactory {
    companion object {
        fun createVision(perpetratorId: PerpetratorId, crimeType: CrimeType): Vision {
            val visionId = VisionId()
            val foreseenAt = LocalDateTime.now().plusHours(2)
            return Vision(
                id = visionId,
                perpetratorId = perpetratorId,
                crimeType = crimeType,
                foreseenAt = foreseenAt
            )
        }
    }
}

@JvmInline
value class VisionId(val value: UUID = UUID.randomUUID()) : Identifier

@DomainEvent
data class CrimeForeseenEvent(
    val visionId: VisionId,
    val perpetratorId: PerpetratorId,
    val crimeType: CrimeType,
    val foreseenAt: LocalDateTime
)

@Repository
interface VisionRepository {
    fun findById(id: VisionId): Vision?
    fun create(vision: Vision)
    fun update(vision: Vision)
}
