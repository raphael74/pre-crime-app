package ch.ejpd.example.precrime.domain

import org.jmolecules.ddd.annotation.ValueObject

@ValueObject
data class AggregateVersion(val value: Long = 0) {
    fun increment(): AggregateVersion = copy(value = value + 1)
}
