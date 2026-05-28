package ch.ejpd.example.precrime.domain.statistic

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class StatisticTest {
    private val statistic = Statistic()

    @Test
    fun `recordPrevention should increment totalCrimesPrevented`() {
        // GIVEN
        val initialCount = statistic.totalCrimesPrevented

        // WHEN
        statistic.recordPrevention()

        // THEN
        assertThat(statistic.totalCrimesPrevented).isEqualTo(initialCount + 1)
    }
}