package org.example.precrime.domain.vision

import org.example.precrime.domain.AggregateVersion
import org.example.precrime.domain.DomainEventPublisher
import org.example.precrime.domain.perpetrator.PerpetratorId
import org.jmolecules.ddd.annotation.AggregateRoot
import org.jmolecules.ddd.annotation.Factory
import org.jmolecules.ddd.annotation.Identity
import org.jmolecules.ddd.annotation.ValueObject
import org.jmolecules.ddd.types.Identifier
import org.jmolecules.event.annotation.DomainEvent
import java.time.OffsetDateTime
import java.util.*

@AggregateRoot
class Vision(
    @Identity val id: VisionId = VisionId(),
    var version: AggregateVersion = AggregateVersion(),
    val perpetratorId: PerpetratorId,
    val crimeType: CrimeType,
    val foreseenAt: OffsetDateTime
) {
    private var _publisher: DomainEventPublisher? = null

    fun injectPublisher(publisher: DomainEventPublisher) {
        this._publisher = publisher
    }

    private val publisher: DomainEventPublisher
        get() = requireNotNull(_publisher) {
            "DomainEventPublisher has not been injected into Vision $id"
        }

    fun foreseeCrime() {
        publisher.publish(
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
            val foreseenAt = OffsetDateTime.now().plusHours(2)
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
    val foreseenAt: OffsetDateTime
)
