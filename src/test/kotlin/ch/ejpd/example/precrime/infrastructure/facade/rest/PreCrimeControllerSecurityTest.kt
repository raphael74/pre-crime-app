package ch.ejpd.example.precrime.infrastructure.facade.rest

import ch.ejpd.example.precrime.IntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.client.RestTestClient

@IntegrationTest
@AutoConfigureRestTestClient
@AutoConfigureMockMvc
class PreCrimeControllerSecurityTest(@Autowired private val restTestClient: RestTestClient) {

    @Test
    fun `access to vision endpoint should be unauthorized without credentials`() {
        restTestClient.post()
            .uri {
                it.path("/api/pre-crime/vision").queryParam("perpetrator", "John Anderton")
                    .queryParam("crimeType", "Future Murder").build()
            }
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `access to vision endpoint should be authorized with correct credentials`() {
        restTestClient.post()
            .uri {
                it.path("/api/pre-crime/vision").queryParam("perpetrator", "John Anderton")
                    .queryParam("crimeType", "Future Murder").build()
            }
            .header("Authorization", generateAuthorizationHeader("precog", "agatha"))
            .exchange()
            .expectStatus().isOk
    }

    private fun generateAuthorizationHeader(username: String, password: String): String {
        return "Basic " + java.util.Base64.getEncoder().encodeToString("${username}:${password}".toByteArray())
    }
}
