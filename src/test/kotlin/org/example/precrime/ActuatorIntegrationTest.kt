package org.example.precrime

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.test.web.servlet.client.RestTestClient

@IntegrationTest
@AutoConfigureRestTestClient
class ActuatorIntegrationTest(@Autowired private val restTestClient: RestTestClient) {

    @Test
    fun `health endpoint should be accessible`() {
        restTestClient.get()
            .uri("/actuator/health")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.status").isEqualTo("UP")
    }

    @Test
    fun `info endpoint should be accessible`() {
        restTestClient.get()
            .uri("/actuator/info")
            .exchange()
            .expectStatus().isOk
    }
}
