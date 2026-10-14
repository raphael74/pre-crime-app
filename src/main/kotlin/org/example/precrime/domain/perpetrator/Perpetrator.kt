package org.example.precrime.domain.perpetrator

import org.example.precrime.domain.AggregateVersion
import org.jmolecules.ddd.annotation.AggregateRoot
import org.jmolecules.ddd.annotation.Identity
import org.jmolecules.ddd.types.Identifier
import java.util.UUID

@AggregateRoot
class Perpetrator(
    @Identity val id: PerpetratorId = PerpetratorId(),
    var version: AggregateVersion = AggregateVersion(),
    val firstName: String,
    val lastName: String,
) {
    init {
        require(firstName.isNotBlank()) { "Perpetrator first name cannot be blank" }
        require(firstName.length <= MAX_NAME_LENGTH) { "Perpetrator first name cannot exceed $MAX_NAME_LENGTH characters" }
        require(lastName.isNotBlank()) { "Perpetrator last name cannot be blank" }
        require(lastName.length <= MAX_NAME_LENGTH) { "Perpetrator last name cannot exceed $MAX_NAME_LENGTH characters" }
    }

    val fullName: String get() = "$firstName $lastName"

    companion object {
        private const val MAX_NAME_LENGTH = 80
    }
}

@JvmInline
value class PerpetratorId(
    val value: UUID = UUID.randomUUID(),
) : Identifier
