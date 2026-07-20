package org.example.precrime.infrastructure.integration.persistence

import org.example.precrime.IntegrationTest
import org.example.precrime.domain.statistic.StatisticId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.util.*

@IntegrationTest
@Transactional
class JooqStatisticRepositoryTest(
    @Autowired private var repository: JooqStatisticRepository
) {

    private val SINGLETON_ID = StatisticId(UUID.fromString("00000000-0000-0000-0000-000000000001"))

    @Test
    fun `should find singleton statistic`() {
        // WHEN
        val statistic = repository.findSingleton()

        // THEN
        assertThat(statistic).isNotNull
        assertThat(statistic.id).isEqualTo(SINGLETON_ID)
    }

    @Test
    fun `should persist total crimes prevented`() {
        // GIVEN
        val statistic = repository.findSingleton()
        statistic.recordPrevention()
        statistic.recordPrevention()

        // WHEN
        repository.update(statistic)

        // THEN
        val result = repository.findSingleton()
        assertThat(result.totalCrimesPrevented).isEqualTo(2)
    }

    @Test
    fun `should return null when finding by non-existing id`() {
        // GIVEN
        val randomId = StatisticId(UUID.randomUUID())

        // WHEN
        val result = repository.findById(randomId)

        // THEN
        assertThat(result).isNull()
    }
}
