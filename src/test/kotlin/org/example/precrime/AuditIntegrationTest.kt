package org.example.precrime

import org.example.precrime.infrastructure.facade.rest.model.ArrestExecutedRequest
import org.example.precrime.infrastructure.facade.rest.model.AuditEntry
import org.example.precrime.infrastructure.facade.rest.model.CreateVisionRequest
import org.example.precrime.infrastructure.facade.rest.model.PreArrestResponse
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.test.web.servlet.client.RestTestClient
import org.springframework.test.web.servlet.client.expectBody
import java.util.*
import java.util.concurrent.TimeUnit

@IntegrationTest
@AutoConfigureRestTestClient
class AuditIntegrationTest(
    @Autowired private val restTestClient: RestTestClient
) {

    @Test
    fun `a foreseen crime and subsequent pre-arrest are logged in the audit log`() {
        // GIVEN: A crime is foreseen
        val firstName = "Danny"
        val lastName = "Witwer"
        triggerVision(firstName, lastName, CreateVisionRequest.CrimeType.ASSAULT)

        // THEN: Both CrimeForeseenEvent and PreArrestExecutedEvent should be in the audit log
        await().pollInterval(1, TimeUnit.SECONDS).atMost(15, TimeUnit.SECONDS).untilAsserted {
            val logs = getAuditLogs()
            assertThat(logs).anyMatch { it.eventType == "CrimeForeseenEvent" && it.payload.contains("perpetratorId") == true }
            assertThat(getPendingPreArrests()).isNotEmpty
        }

        val pendingPreArrests = getPendingPreArrests()
        triggerArrest(pendingPreArrests.first().id)

        await().pollInterval(1, TimeUnit.SECONDS).atMost(15, TimeUnit.SECONDS).untilAsserted {
            val logs = getAuditLogs()
            assertThat(logs).anyMatch { it.eventType == "CrimeForeseenEvent" && it.payload.contains("perpetratorId") == true }
            assertThat(logs).anyMatch { it.eventType == "PreArrestExecutedEvent" && it.payload.contains("perpetratorId") == true }
        }
    }

    private fun triggerVision(firstName: String, lastName: String, crimeType: CreateVisionRequest.CrimeType) {
        restTestClient.post()
            .uri("/api/pre-crime/vision")
            .header("Authorization", generateAuthorizationHeader("precog", "agatha"))
            .body(CreateVisionRequest(lastName, firstName, crimeType))
            .exchange()
            .expectStatus().isCreated
    }

    private fun triggerArrest(preArrestId: UUID) {
        restTestClient.post()
            .uri("/api/pre-crime/arrest-executed")
            .header("Authorization", generateAuthorizationHeader("precog", "agatha"))
            .body(ArrestExecutedRequest(preArrestId))
            .exchange()
            .expectStatus().isOk
    }

    private fun getPendingPreArrests(): List<PreArrestResponse> {
        return restTestClient.get().uri("/api/pre-crime/arrests-pending")
            .header("Authorization", generateAuthorizationHeader("precog", "agatha"))
            .exchange()
            .expectStatus().isOk
            .expectBody<Array<PreArrestResponse>>().returnResult().responseBody?.toList() ?: emptyList()
    }

    private fun getAuditLogs(): List<AuditEntry> {
        return restTestClient.get().uri("/api/audit/logs")
            .header("Authorization", generateAuthorizationHeader("precog", "agatha"))
            .exchange()
            .expectStatus().isOk
            .expectBody<Array<AuditEntry>>().returnResult().responseBody?.toList() ?: emptyList()
    }

    private fun generateAuthorizationHeader(username: String, password: String): String {
        return "Basic " + Base64.getEncoder().encodeToString("${username}:${password}".toByteArray())
    }
}
