package ch.ejpd.example.precrime

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestPropertySource
import org.springframework.transaction.annotation.Transactional


@SpringBootTest
@EmbeddedKafka(partitions = 1)
@TestPropertySource("/application-test.properties")
@Transactional
@DirtiesContext
annotation class IntegrationTest