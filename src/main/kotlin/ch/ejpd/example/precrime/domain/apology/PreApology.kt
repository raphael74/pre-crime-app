package ch.ejpd.example.precrime.domain.apology

import ch.ejpd.example.precrime.domain.DomainEventPublisher
import ch.ejpd.example.precrime.domain.perpetrator.PerpetratorId
import ch.ejpd.example.precrime.domain.vision.VisionId
import org.jmolecules.ddd.annotation.AggregateRoot
import org.jmolecules.ddd.annotation.Identity
import org.jmolecules.ddd.annotation.Repository
import org.jmolecules.ddd.annotation.ValueObject
import org.jmolecules.ddd.types.Identifier
import org.jmolecules.event.annotation.DomainEvent
import java.time.OffsetDateTime
import java.util.*

@AggregateRoot
class PreApology(
    @Identity val id: PreApologyId = PreApologyId(),
    val visionId: VisionId,
    val perpetratorId: PerpetratorId,
    val compensation: Compensation,
    val apologyLetter: ApologyLetter,
    val createdAt: OffsetDateTime = OffsetDateTime.now()
) {
    private var publisher: DomainEventPublisher? = null

    fun injectPublisher(publisher: DomainEventPublisher) {
        this.publisher = publisher
    }

    fun issue() {
        val pub = requireNotNull(publisher) { "DomainEventPublisher has not been injected into PreApology $id" }
        pub.publish(
            PreApologyIssuedEvent(
                apologyId = id,
                visionId = visionId,
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

@ValueObject
data class ApologyLetter(val text: String) {
    init {
        require(text.isNotBlank()) { "Letter text cannot be blank" }
    }
}

@Repository
interface PreApologyRepository {
    fun save(apology: PreApology)
    fun findById(id: PreApologyId): PreApology?
    fun findAll(): List<PreApology>
}

@DomainEvent
data class PreApologyIssuedEvent(
    val apologyId: PreApologyId,
    val visionId: VisionId,
    val perpetratorId: PerpetratorId,
    val netPayout: Double
)
