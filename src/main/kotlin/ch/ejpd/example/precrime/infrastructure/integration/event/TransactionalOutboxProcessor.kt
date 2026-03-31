package ch.ejpd.example.precrime.infrastructure.integration.event

import org.jooq.DSLContext
import org.jooq.impl.DSL.field
import org.jooq.impl.DSL.table
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.*

@Component
class TransactionalOutboxProcessor(
    private val dsl: DSLContext,
    private val kafkaTemplate: KafkaTemplate<String, String>
) {

    @Scheduled(fixedDelay = 1000)
    @Transactional
    fun processOutbox() {
        val records = dsl.selectFrom(table("outbox"))
            .where(field("status").eq("PENDING"))
            .forUpdate()
            .skipLocked()
            .fetch()

        records.forEach { record ->
            val id = record.get("id", UUID::class.java)
            val eventType = record.get("event_type", String::class.java)
            val payload = record.get("payload", String::class.java)

            sendKafkaEvent(eventType, payload)

            dsl.update(table("outbox"))
                .set(field("status", String::class.java), "PROCESSED")
                .set(field("processed_at", OffsetDateTime::class.java), OffsetDateTime.now())
                .where(field("id").eq(id))
                .execute()
        }
    }

    private fun sendKafkaEvent(eventType: String, payload: String) {
        val topic = eventType.substringAfterLast(".")
            .replace(Regex("([a-z])([A-Z])"), "$1-$2")
            .lowercase()
        kafkaTemplate.send(topic, eventType, payload)
    }
}
