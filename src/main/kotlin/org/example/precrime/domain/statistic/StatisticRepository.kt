package org.example.precrime.domain.statistic

import org.jmolecules.ddd.annotation.Repository

@Repository
interface StatisticRepository {
    fun findById(id: StatisticId): Statistic?
    fun update(statistic: Statistic)
    fun findSingleton(): Statistic
}