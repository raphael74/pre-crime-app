package org.example.precrime.domain.perpetrator

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class PerpetratorTest {

    @Test
    fun `should throw exception when first name exceeds 80 characters`() {
        val longFirstName = "A".repeat(81)
        assertThatThrownBy {
            Perpetrator(firstName = longFirstName, lastName = "Doe")
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Perpetrator first name cannot exceed 80 characters")
    }

    @Test
    fun `should throw exception when last name exceeds 80 characters`() {
        val longLastName = "A".repeat(81)
        assertThatThrownBy {
            Perpetrator(firstName = "John", lastName = longLastName)
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Perpetrator last name cannot exceed 80 characters")
    }

    @Test
    fun `should create perpetrator when names are exactly 80 characters`() {
        val name80 = "A".repeat(80)
        val perpetrator = Perpetrator(firstName = name80, lastName = name80)
        org.assertj.core.api.Assertions.assertThat(perpetrator.firstName).hasSize(80)
        org.assertj.core.api.Assertions.assertThat(perpetrator.lastName).hasSize(80)
    }
}
