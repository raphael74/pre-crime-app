package ch.ejpd.example.precrime

import ch.ejpd.example.precrime.infrastructure.KafkaTopics.Companion.CRIME_FORESEEN_EVENT_TOPIC
import ch.ejpd.example.precrime.infrastructure.KafkaTopics.Companion.PRE_ARREST_EXECUTED_EVENT_TOPIC
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles


@SpringBootTest
@EmbeddedKafka(
    topics = [CRIME_FORESEEN_EVENT_TOPIC, PRE_ARREST_EXECUTED_EVENT_TOPIC],
    partitions = 1,
    controlledShutdown = true
)
@ActiveProfiles("integration-test")
@DirtiesContext
annotation class IntegrationTest