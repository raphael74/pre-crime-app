package ch.ejpd.example.precrime.domain.statistic

import ch.ejpd.example.precrime.domain.AggregateVersion
import org.jmolecules.ddd.annotation.AggregateRoot
import org.jmolecules.ddd.annotation.Identity
import org.jmolecules.ddd.types.Identifier
import java.util.*

@AggregateRoot
class Statistic(
    @Identity val id: StatisticId = StatisticId(),
    var version: AggregateVersion = AggregateVersion(),
    var totalCrimesPrevented: Int = 0,
) {
    fun recordPrevention() {
        this.totalCrimesPrevented++
    }
}

@JvmInline
value class StatisticId(val value: UUID = UUID.randomUUID()) : Identifier
