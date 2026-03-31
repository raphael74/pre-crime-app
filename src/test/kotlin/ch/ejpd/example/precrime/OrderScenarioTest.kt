package ch.ejpd.example.precrime

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.client.RestTestClient

@SpringBootTest
@EmbeddedKafka(topics = ["domain-events"])
@TestPropertySource("/application-test.properties")
@AutoConfigureRestTestClient
class OrderScenarioTest(@Autowired private val restTestClient: RestTestClient) {
    /*
        @Autowired
        lateinit var dsl: DSLContext

        @Autowired
        lateinit var embeddedKafka: EmbeddedKafkaBroker

        @Test
        fun `should create order and publish event via outbox`() {
            // Given
            val customerId = UUID.randomUUID()
            val productId = UUID.randomUUID()
            val request = OrderController.CreateOrderRequest(
                customerId = customerId,
                items = listOf(OrderController.OrderItemDto(productId, 2, BigDecimal("100.00")))
            )

            // When
            val response = restTestClient.post()
                .uri("/api/orders")
                .body(request)
                .exchange()
                .expectStatus().isOk
                .expectBody(OrderController.CreateOrderResponse::class.java)
                .returnResult()
                .responseBody ?: throw IllegalStateException("Order response body missing")

            val orderId = response.id

            // Verify Database
            val orderRecord = dsl.selectFrom(table("ORDERS")).where(field("ID").eq(orderId)).fetch().firstOrNull()
            assertEquals(customerId, orderRecord?.get("CUSTOMER_ID"))

            // Verify Kafka Event via Awaitility (for Outbox Processor)
            val consumer = createKafkaConsumer()
            consumer.subscribe(listOf("domain-events"))

            await().atMost(15, TimeUnit.SECONDS).untilAsserted {
                val records = consumer.poll(Duration.ofMillis(500))
                val found = records.any { it.value().contains(orderId.toString()) }
                assertEquals(true, found, "Expected to find event for order $orderId in Kafka")
            }
        }

        private fun createKafkaConsumer(): KafkaConsumer<String, String> {
            val props = Properties()
            props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = embeddedKafka.brokersAsString
            props[ConsumerConfig.GROUP_ID_CONFIG] = "test-group-" + UUID.randomUUID()
            props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
            props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
            props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
            return KafkaConsumer(props)
        }
     */
}
