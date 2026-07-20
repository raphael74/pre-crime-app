package org.example.precrime

import org.example.precrime.domain.statistic.StatisticRepository
import org.example.precrime.infrastructure.integration.persistence.OptimisticLockingException
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

@IntegrationTest
class OptimisticLockingTest(
    @Autowired private val statisticRepository: StatisticRepository
) {

    @Test
    @Transactional
    fun `concurrent update should throw OptimisticLockingException`() {
        // GIVEN
        val statistic1 = statisticRepository.findSingleton()
        val statistic2 = statisticRepository.findSingleton()

        // WHEN
        statistic1.recordPrevention()
        statisticRepository.update(statistic1)

        // THEN
        statistic2.recordPrevention()
        assertThrows(OptimisticLockingException::class.java) {
            statisticRepository.update(statistic2)
        }
    }
}
