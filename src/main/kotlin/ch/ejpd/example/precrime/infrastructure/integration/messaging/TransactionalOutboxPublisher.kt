package ch.ejpd.example.precrime.infrastructure.integration.messaging

import ch.ejpd.example.precrime.application.event.DomainEventPublisher
import org.jooq.DSLContext
import org.jooq.impl.DSL.field
import org.jooq.impl.DSL.table
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import java.util.*

@Component
class TransactionalOutboxPublisher(
    private val dsl: DSLContext,
    private val objectMapper: ObjectMapper
) : DomainEventPublisher {

    @org.jmolecules.event.annotation.DomainEventPublisher
    override fun publish(event: Any) {
        val payload = objectMapper.writeValueAsString(event)
        val eventType = event::class.java.name

        dsl.insertInto(table("outbox"))
            .set(field("id", UUID::class.java), UUID.randomUUID())
            .set(field("event_type", String::class.java), eventType)
            .set(field("payload", String::class.java), payload)
            .set(field("status", String::class.java), "PENDING")
            .execute()
    }
}
