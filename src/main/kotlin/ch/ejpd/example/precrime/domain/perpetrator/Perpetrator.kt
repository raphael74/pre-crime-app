package ch.ejpd.example.precrime.domain.perpetrator

import ch.ejpd.example.precrime.domain.AggregateVersion
import org.jmolecules.ddd.annotation.AggregateRoot
import org.jmolecules.ddd.annotation.Identity
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
        require(firstName.length <= 80) { "Perpetrator first name cannot exceed 80 characters" }
        require(lastName.isNotBlank()) { "Perpetrator last name cannot be blank" }
        require(lastName.length <= 80) { "Perpetrator last name cannot exceed 80 characters" }
    }

    val fullName: String get() = "$firstName $lastName"
}

@JvmInline
value class PerpetratorId(val value: UUID = UUID.randomUUID()) : Identifier
