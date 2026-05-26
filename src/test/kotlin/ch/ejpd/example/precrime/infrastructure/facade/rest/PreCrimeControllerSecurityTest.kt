package ch.ejpd.example.precrime.infrastructure.facade.rest

import ch.ejpd.example.precrime.IntegrationTest
import ch.ejpd.example.precrime.infrastructure.facade.rest.model.CreateVisionRequest
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
            .uri("/api/pre-crime/vision")
            .body(CreateVisionRequest("John Anderton", CreateVisionRequest.CrimeType.MURDER))
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `access to vision endpoint should be authorized with correct credentials`() {
        restTestClient.post()
            .uri("/api/pre-crime/vision")
            .body(CreateVisionRequest("John Anderton", CreateVisionRequest.CrimeType.MURDER))
            .header("Authorization", generateAuthorizationHeader("precog", "agatha"))
            .exchange()
            .expectStatus().isCreated
    }

    private fun generateAuthorizationHeader(username: String, password: String): String {
        return "Basic " + java.util.Base64.getEncoder().encodeToString("${username}:${password}".toByteArray())
    }
}
