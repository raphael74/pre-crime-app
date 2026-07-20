package org.example.precrime.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AggregateVersionTest {

    @Test
    fun `default version should be zero`() {
        val version = AggregateVersion()
        assertThat(version.value).isZero()
    }

    @Test
    fun `increment should return new version with incremented value`() {
        val version = AggregateVersion(5)
        val incremented = version.increment()

        assertThat(incremented.value).isEqualTo(6)
    }

    @Test
    fun `increment should not mutate original version`() {
        val version = AggregateVersion(5)
        version.increment()

        assertThat(version.value).isEqualTo(5)
    }
}
