package org.example.precrime.infrastructure

class KafkaTopics {
    companion object {
        const val CRIME_FORESEEN_EVENT_TOPIC = "crime-foreseen-event"
        const val PRE_ARREST_EXECUTED_EVENT_TOPIC = "pre-arrest-executed-event"
        const val PRE_ARREST_CANCELLED_EVENT_TOPIC = "pre-arrest-cancelled-event"
        const val PRE_APOLOGY_ISSUED_EVENT_TOPIC = "pre-apology-issued-event"
    }
}