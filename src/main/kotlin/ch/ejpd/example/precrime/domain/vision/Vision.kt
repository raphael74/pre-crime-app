package ch.ejpd.example.precrime.domain.vision

import ch.ejpd.example.precrime.domain.AggregateVersion
import org.jmolecules.ddd.annotation.*
import org.jmolecules.ddd.types.Identifier
import org.jmolecules.event.annotation.DomainEvent
import java.time.LocalDateTime
import java.util.*

@AggregateRoot
class Vision(
    @Identity val id: VisionId,
    var version: AggregateVersion = AggregateVersion(),
    val perpetrator: Perpetrator,
    val crimeType: CrimeType,
    val foreseenAt: LocalDateTime
) {
    private val _events = mutableListOf<Any>()

    val domainEvents: List<Any> get() = _events.toList()

    fun clearDomainEvents() {
        _events.clear()
    }

    fun foreseeCrime() {
        _events.add(
            CrimeForeseenEvent(
                visionId = id,
                perpetrator = perpetrator,
                crimeType = crimeType,
                foreseenAt = foreseenAt
            )
        )
    }
}

@ValueObject
data class Perpetrator(val firstName: String, val lastName: String) {
    init {
        require(firstName.isNotBlank()) { "Perpetrator first name cannot be blank" }
        require(lastName.isNotBlank()) { "Perpetrator last name cannot be blank" }
    }

    val fullName: String get() = "$firstName $lastName"
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
        fun createVision(perpetrator: Perpetrator, crimeType: CrimeType): Vision {
            val visionId = VisionId()
            val foreseenAt = LocalDateTime.now().plusHours(2)
            return Vision(
                id = visionId,
                perpetrator = perpetrator,
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
    val perpetrator: Perpetrator,
    val crimeType: CrimeType,
    val foreseenAt: LocalDateTime
)

@Repository
interface VisionRepository {
    fun findById(id: VisionId): Vision?
    fun create(vision: Vision)
    fun update(vision: Vision)
}
