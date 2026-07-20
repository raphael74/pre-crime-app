package org.example.precrime.domain

import org.jmolecules.ddd.annotation.ValueObject

@ValueObject
@JvmInline
value class AggregateVersion(val value: Long = 0) {
    fun increment(): AggregateVersion = AggregateVersion(value + 1)
}
