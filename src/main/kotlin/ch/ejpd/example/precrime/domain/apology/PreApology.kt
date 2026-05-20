package ch.ejpd.example.precrime.domain.apology

import ch.ejpd.example.precrime.domain.precog.Perpetrator
import ch.ejpd.example.precrime.domain.precog.VisionId
import org.jmolecules.ddd.annotation.AggregateRoot
import org.jmolecules.ddd.annotation.Identity
import org.jmolecules.ddd.annotation.Repository
import org.jmolecules.ddd.annotation.ValueObject
import org.jmolecules.ddd.types.Identifier
import java.util.*

@AggregateRoot
class PreApology(
    @Identity val id: PreApologyId = PreApologyId(),
    val visionId: VisionId,
    val perpetrator: Perpetrator,
    val compensation: Compensation,
    val apologyLetter: ApologyLetter
) {
    private val _events = mutableListOf<Any>()
    val domainEvents: List<Any> get() = _events.toList()

    fun clearDomainEvents() {
        _events.clear()
    }

    fun issue() {
        _events.add(
            PreApologyIssuedEvent(
                apologyId = id,
                visionId = visionId,
                perpetrator = perpetrator,
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
