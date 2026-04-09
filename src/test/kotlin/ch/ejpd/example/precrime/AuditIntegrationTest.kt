package ch.ejpd.example.precrime

import ch.ejpd.example.precrime.domain.audit.AuditEntry
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.core.ParameterizedTypeReference
import org.springframework.test.web.servlet.client.RestTestClient
import java.util.*
import java.util.concurrent.TimeUnit

@IntegrationTest
@AutoConfigureRestTestClient
@AutoConfigureMockMvc
class AuditIntegrationTest(
    @Autowired private val restTestClient: RestTestClient
) {

    @Test
    fun `a foreseen crime and subsequent pre-arrest are logged in the audit log`() {
        // GIVEN: A crime is foreseen
        val perpetrator = "Danny Witwer"
        val crimeType = "Justice Interruption"
        triggerVision(perpetrator, crimeType)

        // THEN: Both CrimeForeseenEvent and PreArrestExecutedEvent should be in the audit log
        await().pollInterval(1, TimeUnit.SECONDS).atMost(15, TimeUnit.SECONDS).untilAsserted {
            val logs = getAuditLogs()
            assertThat(logs).anyMatch { it.eventType == "CrimeForeseenEvent" && it.payload.contains(perpetrator) }
            assertThat(logs).anyMatch { it.eventType == "PreArrestExecutedEvent" && it.payload.contains(perpetrator) }
        }
    }

    private fun triggerVision(perpetrator: String, crimeType: String) {
        restTestClient.post()
            .uri {
                it.path("/api/pre-crime/vision")
                    .queryParam("perpetrator", perpetrator)
                    .queryParam("crimeType", crimeType)
                    .build()
            }
            .header("Authorization", generateAuthorizationHeader("precog", "agatha"))
            .exchange()
            .expectStatus().isOk
    }

    private fun getAuditLogs(): List<AuditEntry> {
        return restTestClient.get().uri("/api/audit/logs")
            .header("Authorization", generateAuthorizationHeader("precog", "agatha"))
            .exchange()
            .expectStatus().isOk
            .expectBody(object : ParameterizedTypeReference<List<AuditEntry>>() {}).returnResult().responseBody
            ?: emptyList()
    }

    private fun generateAuthorizationHeader(username: String, password: String): String {
        return "Basic " + Base64.getEncoder().encodeToString("${username}:${password}".toByteArray())
    }
}
