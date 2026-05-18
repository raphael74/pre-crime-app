package ch.ejpd.example.precrime

import ch.ejpd.example.precrime.domain.enforcement.LawEnforcementRepository
import ch.ejpd.example.precrime.domain.precog.PrecogDivisionRepository
import ch.ejpd.example.precrime.infrastructure.facade.rest.CreateVisionRequest
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
        val perpetrator = "John Doe"
        val crimeType = "Future Murder"
        triggerVision(perpetrator, crimeType)

        // THEN: The statistics should be updated via the bidirectional event flow
        await().pollInterval(1, TimeUnit.SECONDS).atMost(10, TimeUnit.SECONDS).untilAsserted {
            val updatedCrimeCount = getStats()
            assertThat(updatedCrimeCount).isEqualTo(initialCrimeCount + 1)

            // AND: The visions and pre-arrests are persisted in the aggregates
            val division = precogRepository.findSingleton()
            assertThat(division.visions).anyMatch { it.perpetrator.name == perpetrator && it.crimeType.value == crimeType }

            val unit = enforcementRepository.findSingleton()
            assertThat(unit.preArrests).anyMatch { it.perpetrator.name == perpetrator }
        }
    }

    private fun getStats(): Int {
        return restTestClient.get().uri("/api/pre-crime/stats")
            .header("Authorization", generateAuthorizationHeader("precog", "agatha"))
            .exchange()
            .expectStatus().isOk
            .expectBody(Int::class.java).returnResult().responseBody ?: 0
    }

    private fun triggerVision(perpetrator: String, crimeType: String) {
        restTestClient.post()
            .uri("/api/pre-crime/vision")
            .header("Authorization", generateAuthorizationHeader("precog", "agatha"))
            .body(CreateVisionRequest(perpetrator, crimeType))
            .exchange()
            .expectStatus().isCreated
    }

    private fun generateAuthorizationHeader(username: String, password: String): String {
        return "Basic " + Base64.getEncoder().encodeToString("${username}:${password}".toByteArray())
    }
}
