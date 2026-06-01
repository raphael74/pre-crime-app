package ch.ejpd.example.precrime.domain.perpetrator

import ch.ejpd.example.precrime.domain.AggregateVersion
import org.jmolecules.ddd.annotation.AggregateRoot
import org.jmolecules.ddd.annotation.Identity
import org.jmolecules.ddd.annotation.Repository
import org.jmolecules.ddd.types.Identifier
import java.util.*

@AggregateRoot
class Perpetrator(
    @Identity val id: PerpetratorId = PerpetratorId(),
    var version: AggregateVersion = AggregateVersion(),
    val firstName: String,
    val lastName: String
) {
    init {
        require(firstName.isNotBlank()) { "Perpetrator first name cannot be blank" }
        require(lastName.isNotBlank()) { "Perpetrator last name cannot be blank" }
    }

    val fullName: String get() = "$firstName $lastName"
}

@JvmInline
value class PerpetratorId(val value: UUID = UUID.randomUUID()) : Identifier

@Repository
interface PerpetratorRepository {
    fun findById(id: PerpetratorId): Perpetrator?
    fun findByIds(ids: Collection<PerpetratorId>): List<Perpetrator>
    fun findByFirstAndLastName(firstName: String, lastName: String): Perpetrator?
    fun save(perpetrator: Perpetrator)
    fun findAll(): List<Perpetrator>
}
