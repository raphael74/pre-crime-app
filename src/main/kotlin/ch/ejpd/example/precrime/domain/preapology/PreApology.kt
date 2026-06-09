package ch.ejpd.example.precrime.domain.preapology

import ch.ejpd.example.precrime.domain.DomainEventPublisher
import ch.ejpd.example.precrime.domain.perpetrator.PerpetratorId
import ch.ejpd.example.precrime.domain.prearrest.PreArrestId
import ch.ejpd.example.precrime.domain.vision.CrimeType
import org.jmolecules.ddd.annotation.AggregateRoot
import org.jmolecules.ddd.annotation.Factory
import org.jmolecules.ddd.annotation.Identity
import org.jmolecules.ddd.annotation.ValueObject
import org.jmolecules.ddd.types.Identifier
import org.jmolecules.event.annotation.DomainEvent
import java.time.OffsetDateTime
import java.util.*

@AggregateRoot
class PreApology(
    @Identity val id: PreApologyId = PreApologyId(),
    val preArrestId: PreArrestId,
    val perpetratorId: PerpetratorId,
    val compensation: Compensation,
    val apologyLetter: ApologyLetter,
    val createdAt: OffsetDateTime = OffsetDateTime.now()
) {
    private var _publisher: DomainEventPublisher? = null

    fun injectPublisher(publisher: DomainEventPublisher) {
        this._publisher = publisher
    }

    private val publisher: DomainEventPublisher
        get() = requireNotNull(_publisher) {
            "DomainEventPublisher has not been injected into PreApology $id"
        }

    fun issue() {
        publisher.publish(
            PreApologyIssuedEvent(
                apologyId = id,
                preArrestId = preArrestId,
                perpetratorId = perpetratorId,
                netPayout = compensation.netPayout
            )
        )
    }
}

@JvmInline
value class PreApologyId(val value: UUID = UUID.randomUUID()) : Identifier

@ValueObject
data class Compensation(
    val baseAmount: Double,
    val jetpackFuelDeduction: Double,
    val haloRentalFee: Double,
    val netPayout: Double
) {
    fun isBillableToFamily(): Boolean = netPayout < 0
}

@Factory
class CompensationFactory {
    companion object {
        fun createCompensation(crimeType: CrimeType): Compensation {

            val baseAmount = when (crimeType) {
                CrimeType.MURDER -> 10000.0
                CrimeType.GRAND_THEFT_AUTO -> 5000.0
                CrimeType.JAYWALKING -> 50.0
                else -> 1000.0
            }

            val jetpackFuelDeduction = 450.0 // Standard deployment fee
            val haloRentalFee = 250.0       // Stasis halo overnight fee
            val netPayout = baseAmount - jetpackFuelDeduction - haloRentalFee

            return Compensation(baseAmount, jetpackFuelDeduction, haloRentalFee, netPayout)
        }
    }
}

@ValueObject
data class ApologyLetter(val text: String) {
    init {
        require(text.isNotBlank()) { "Letter text cannot be blank" }
    }
}

@DomainEvent
data class PreApologyIssuedEvent(
    val apologyId: PreApologyId,
    val preArrestId: PreArrestId,
    val perpetratorId: PerpetratorId,
    val netPayout: Double
)
