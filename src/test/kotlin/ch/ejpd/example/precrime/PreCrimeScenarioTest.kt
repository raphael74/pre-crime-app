package ch.ejpd.example.precrime

import ch.ejpd.example.precrime.domain.enforcement.LawEnforcementRepository
import ch.ejpd.example.precrime.domain.precog.PrecogDivisionRepository
import ch.ejpd.example.precrime.infrastructure.facade.rest.model.CreateVisionRequest
import ch.ejpd.example.precrime.infrastructure.facade.rest.model.PreApologyResponse
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.client.RestTestClient
import java.util.*
import java.util.concurrent.TimeUnit

@IntegrationTest
@AutoConfigureRestTestClient
@AutoConfigureMockMvc
class PreCrimeScenarioTest(
    @Autowired private val restTestClient: RestTestClient,
    @Autowired private val precogRepository: PrecogDivisionRepository,
    @Autowired private val enforcementRepository: LawEnforcementRepository
) {

    @Test
    fun `a future crime foreseen results in a pre-arrest and updated stats`() {
        // GIVEN: The department is operational
        val initialCrimeCount = getStats()

        // WHEN: Precogs foresee a crime
        val firstName = "John"
        val lastName = "Doe"
        val crimeType =
            CreateVisionRequest.CrimeType.MURDER // Using "Murder" to trigger base amount calculations correctly
        triggerVision(firstName, lastName, crimeType)

        // THEN: The statistics should be updated via the bidirectional event flow
        await().pollInterval(1, TimeUnit.SECONDS).atMost(10, TimeUnit.SECONDS).untilAsserted {
            val updatedCrimeCount = getStats()
            assertThat(updatedCrimeCount).isEqualTo(initialCrimeCount + 1)

            // AND: The visions and pre-arrests are persisted in the aggregates
            val division = precogRepository.findSingleton()
            assertThat(division.visions).anyMatch { it.perpetrator.fullName == "$firstName $lastName" && it.crimeType.name == crimeType.value }

            val unit = enforcementRepository.findSingleton()
            assertThat(unit.preArrests).anyMatch { it.perpetrator.fullName == "$firstName $lastName" }

            // AND: A pre-emptive apology is generated and retrievable via REST API
            val apologies = getApologies()
            assertThat(apologies).hasSize(1)
            val apology = apologies.first()
            assertThat(apology.lastName).isEqualTo(lastName)
            assertThat(apology.firstName).isEqualTo(firstName)
            assertThat(apology.baseAmount?.toDouble()).isEqualTo(10000.0)
            assertThat(apology.jetpackFuelDeduction?.toDouble()).isEqualTo(450.0)
            assertThat(apology.haloRentalFee?.toDouble()).isEqualTo(250.0)
            assertThat(apology.netPayout?.toDouble()).isEqualTo(9300.0)
            assertThat(apology.apologyText).contains("Dear Family of John Doe")
        }
    }

    private fun getStats(): Int {
        return restTestClient.get().uri("/api/pre-crime/stats")
            .header("Authorization", generateAuthorizationHeader("precog", "agatha"))
            .exchange()
            .expectStatus().isOk
            .expectBody(Int::class.java).returnResult().responseBody ?: 0
    }

    private fun getApologies(): List<PreApologyResponse> {
        return restTestClient.get().uri("/api/pre-crime/apologies")
            .header("Authorization", generateAuthorizationHeader("precog", "agatha"))
            .exchange()
            .expectStatus().isOk
            .expectBody(Array<PreApologyResponse>::class.java).returnResult().responseBody?.toList() ?: emptyList()
    }

    private fun triggerVision(firstName: String, lastName: String, crimeType: CreateVisionRequest.CrimeType) {
        restTestClient.post()
            .uri("/api/pre-crime/vision")
            .header("Authorization", generateAuthorizationHeader("precog", "agatha"))
            .body(CreateVisionRequest(lastName, firstName, crimeType))
            .exchange()
            .expectStatus().isCreated
    }

    private fun generateAuthorizationHeader(username: String, password: String): String {
        return "Basic " + Base64.getEncoder().encodeToString("${username}:${password}".toByteArray())
    }
}
