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
        val records = dsl.selectFrom(table("OUTBOX"))
            .where(field("STATUS").eq("PENDING"))
            .forUpdate()
            .skipLocked()
            .fetch()

        records.forEach { record ->
            val id = record.get("ID", UUID::class.java)
            val eventType = record.get("EVENT_TYPE", String::class.java)
            val payload = record.get("PAYLOAD", String::class.java)

            sendKafkaEvent(eventType, payload)

            dsl.update(table("OUTBOX"))
                .set(field("STATUS", String::class.java), "PROCESSED")
                .set(field("PROCESSED_AT", OffsetDateTime::class.java), OffsetDateTime.now())
                .where(field("ID").eq(id))
                .execute()
        }
    }

    private fun sendKafkaEvent(key: String, payload: String) {
        kafkaTemplate.send("domain-events", key, payload)
    }
}
