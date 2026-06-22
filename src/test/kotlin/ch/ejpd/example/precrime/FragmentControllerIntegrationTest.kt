package ch.ejpd.example.precrime

import ch.ejpd.example.precrime.infrastructure.facade.rest.model.ArrestExecutedRequest
import ch.ejpd.example.precrime.infrastructure.facade.rest.model.CreateVisionRequest
import ch.ejpd.example.precrime.infrastructure.facade.rest.model.PreArrestResponse
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.test.web.servlet.client.RestTestClient
import org.springframework.test.web.servlet.client.expectBody
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.TimeUnit

@IntegrationTest
@AutoConfigureRestTestClient
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class FragmentControllerIntegrationTest(
    @Autowired private val restTestClient: RestTestClient
) {

    @Test
    @Order(1)
    fun `empty fragments show correct empty state messages`() {
        val pendingHtml = getFragment("/fragments/pending-pre-arrests")
        assertThat(pendingHtml).contains("No pending Pre-Arrests")

        val executedHtml = getFragment("/fragments/executed-pre-arrests")
        assertThat(executedHtml).contains("No executed Pre-Arrests")

        val apologiesHtml = getFragment("/fragments/apologies")
        assertThat(apologiesHtml).contains("NO APOLOGIES GENERATED YET")
    }

    @Test
    @Order(2)
    fun `fragment endpoints reflect the full pre-crime lifecycle`() {
        val statsHtml = getFragment("/fragments/stats")
        assertThat(statsHtml).contains("0")

        val firstName = "John"
        val lastName = "Doe"
        triggerVision(firstName, lastName, CreateVisionRequest.CrimeType.MURDER)

        await().pollInterval(1, TimeUnit.SECONDS).atMost(10, TimeUnit.SECONDS).untilAsserted {
            val pendingHtml = getFragment("/fragments/pending-pre-arrests")
            assertThat(pendingHtml).contains("$firstName $lastName")
            assertThat(pendingHtml).contains("PENDING")
        }

        val pendingPreArrests = getPendingPreArrests()
        triggerArrest(pendingPreArrests.first().id)

        await().pollInterval(1, TimeUnit.SECONDS).atMost(10, TimeUnit.SECONDS).untilAsserted {
            val executedHtml = getFragment("/fragments/executed-pre-arrests")
            assertThat(executedHtml).contains("$firstName $lastName")
            assertThat(executedHtml).contains("ARRESTED_BEFORE_CRIME")
        }

        await().pollInterval(1, TimeUnit.SECONDS).atMost(10, TimeUnit.SECONDS).untilAsserted {
            val statsHtmlAfter = getFragment("/fragments/stats")
            assertThat(statsHtmlAfter).doesNotContain("0")
            assertThat(statsHtmlAfter).contains("1")
        }

        await().pollInterval(1, TimeUnit.SECONDS).atMost(10, TimeUnit.SECONDS).untilAsserted {
            val apologiesHtml = getFragment("/fragments/apologies")
            assertThat(apologiesHtml).contains("Dear Family of $firstName $lastName")
            assertThat(apologiesHtml).contains("+")
        }
    }

    @Test
    @Order(3)
    fun `audit-logs fragment returns surveillance data`() {
        val firstName = "Danny"
        val lastName = "Witwer"
        triggerVision(firstName, lastName, CreateVisionRequest.CrimeType.ASSAULT)

        await().pollInterval(1, TimeUnit.SECONDS).atMost(10, TimeUnit.SECONDS).untilAsserted {
            val auditHtml = getFragment("/fragments/audit-logs")
            assertThat(auditHtml).contains("CrimeForeseenEvent")
            assertThat(auditHtml).contains("perpetratorId")
        }
    }

    private fun getFragment(path: String): String {
        val result = restTestClient.get()
            .uri(path)
            .header("Authorization", generateAuthorizationHeader("precog", "agatha"))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .returnResult()
        return String(result.responseBody ?: ByteArray(0), StandardCharsets.UTF_8)
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

    private fun generateAuthorizationHeader(username: String, password: String): String {
        return "Basic " + Base64.getEncoder().encodeToString("${username}:${password}".toByteArray())
    }
}
