package ch.ejpd.example.precrime.domain.preapology

import ch.ejpd.example.precrime.domain.vision.CrimeType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource

class CompensationFactoryTest {

    @ParameterizedTest
    @MethodSource("compensationExpectations")
    fun `should compute correct compensation for each crime type`(
        crimeType: CrimeType,
        expectedBaseAmount: Double,
        expectedNetPayout: Double,
        expectedBillable: Boolean
    ) {
        val compensation = CompensationFactory.createCompensation(crimeType)

        assertThat(compensation.baseAmount).isEqualTo(expectedBaseAmount)
        assertThat(compensation.netPayout).isEqualTo(expectedNetPayout)
        assertThat(compensation.isBillableToFamily()).isEqualTo(expectedBillable)
    }

    @ParameterizedTest
    @EnumSource(CrimeType::class)
    fun `every crime type should have the same deduction fees`(crimeType: CrimeType) {
        val compensation = CompensationFactory.createCompensation(crimeType)

        assertThat(compensation.jetpackFuelDeduction).isEqualTo(450.0)
        assertThat(compensation.haloRentalFee).isEqualTo(250.0)
    }

    companion object {
        @JvmStatic
        fun compensationExpectations(): List<Arguments> = listOf(
            Arguments.of(CrimeType.MURDER, 10000.0, 9300.0, false),
            Arguments.of(CrimeType.GRAND_THEFT_AUTO, 5000.0, 4300.0, false),
            Arguments.of(CrimeType.JAYWALKING, 50.0, -650.0, true),
            Arguments.of(CrimeType.THEFT, 1000.0, 300.0, false),
            Arguments.of(CrimeType.ASSAULT, 1000.0, 300.0, false),
            Arguments.of(CrimeType.ROBBERY, 1000.0, 300.0, false),
            Arguments.of(CrimeType.BURGLARY, 1000.0, 300.0, false),
            Arguments.of(CrimeType.FRAUD, 1000.0, 300.0, false),
            Arguments.of(CrimeType.ARSON, 1000.0, 300.0, false),
            Arguments.of(CrimeType.KIDNAPPING, 1000.0, 300.0, false),
            Arguments.of(CrimeType.LARCENY, 1000.0, 300.0, false),
            Arguments.of(CrimeType.VANDALISM, 1000.0, 300.0, false),
            Arguments.of(CrimeType.IDENTITY_THEFT, 1000.0, 300.0, false),
            Arguments.of(CrimeType.TAX_EVASION, 1000.0, 300.0, false),
        )
    }
}
