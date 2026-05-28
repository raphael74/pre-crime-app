package ch.ejpd.example.precrime.infrastructure.integration.persistence

import ch.ejpd.example.precrime.domain.statistic.Statistic
import ch.ejpd.example.precrime.domain.statistic.StatisticId
import ch.ejpd.example.precrime.domain.statistic.StatisticRepository
import ch.ejpd.example.precrime.infrastructure.integration.persistence.jooq.tables.references.STATISTIC
import org.jooq.DSLContext
import org.jooq.Record
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Component
class JooqStatisticRepository(
    private val dsl: DSLContext
) : StatisticRepository {

    companion object {
        private const val STATISTIC_ID_STRING = "00000000-0000-0000-0000-000000000001"
    }

    override fun findById(id: StatisticId): Statistic? {
        return dsl.select(STATISTIC.ID, STATISTIC.TOTAL_CRIMES_PREVENTED, STATISTIC.VERSION)
            .from(STATISTIC)
            .where(STATISTIC.ID.eq(id))
            .fetchOne()
            ?.toStatistic()
    }

    @Transactional(propagation = Propagation.MANDATORY)
    override fun update(statistic: Statistic) {
        val updatedRows = dsl.update(STATISTIC)
            .set(STATISTIC.TOTAL_CRIMES_PREVENTED, statistic.totalCrimesPrevented)
            .set(STATISTIC.VERSION, statistic.version.increment())
            .where(STATISTIC.ID.eq(statistic.id))
            .and(STATISTIC.VERSION.eq(statistic.version))
            .execute()

        if (updatedRows == 0) {
            throw OptimisticLockingException("Statistic with ID ${statistic.id} was updated or deleted by another transaction")
        }

        statistic.version = statistic.version.increment()
    }

    override fun findSingleton(): Statistic = findById(StatisticId(UUID.fromString(STATISTIC_ID_STRING)))!!

    private fun Record.toStatistic() = Statistic(
        get(STATISTIC.ID)!!,
        get(STATISTIC.VERSION)!!,
        get(STATISTIC.TOTAL_CRIMES_PREVENTED)!!
    )

}
