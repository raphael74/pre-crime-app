package ch.ejpd.example.precrime.infrastructure.integration.persistence

import ch.ejpd.example.precrime.domain.DomainEventPublisher
import ch.ejpd.example.precrime.domain.precog.*
import org.jooq.DSLContext
import org.jooq.impl.DSL.field
import org.jooq.impl.DSL.table
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*

@Component
class JooqPrecogDivisionRepository(
    private val dsl: DSLContext,
    private val publisher: DomainEventPublisher
) : PrecogDivisionRepository {
    private val PRECOG_TABLE = table("precog_division")
    private val VISION_TABLE = table("vision")

    private val ID_COL = uuidField("id", PrecogDivisionId::class.java, ::PrecogDivisionId, PrecogDivisionId::value)
    private val STATS_COL = field("total_crimes_prevented", Int::class.java)
    private val VERSION_COL = field("version", Long::class.java)

    private val VISION_ID_COL = uuidField("id", VisionId::class.java, ::VisionId, VisionId::value)
    private val PRECOG_ID_COL =
        uuidField("precog_division_id", PrecogDivisionId::class.java, ::PrecogDivisionId, PrecogDivisionId::value)
    private val PERPETRATOR_COL = field("perpetrator", String::class.java)
    private val CRIME_TYPE_COL = field("crime_type", String::class.java)
    private val FORESEEN_AT_COL = field("foreseen_at", LocalDateTime::class.java)

    private val SINGLETON_ID = PrecogDivisionId(UUID.fromString("00000000-0000-0000-0000-000000000001"))

    override fun findById(id: PrecogDivisionId): PrecogDivision? {
        val record = dsl.select(ID_COL, STATS_COL, VERSION_COL)
            .from(PRECOG_TABLE)
            .where(ID_COL.eq(id))
            .fetchOne() ?: return null

        val visions = dsl.select(VISION_ID_COL, PERPETRATOR_COL, CRIME_TYPE_COL, FORESEEN_AT_COL)
            .from(VISION_TABLE)
            .where(PRECOG_ID_COL.eq(id))
            .fetch()
            .map { r ->
                Vision(r.get(VISION_ID_COL), r.get(PERPETRATOR_COL), r.get(CRIME_TYPE_COL), r.get(FORESEEN_AT_COL))
            }

        val division = PrecogDivision(
            record.get(ID_COL),
            record.get(VERSION_COL),
            record.get(STATS_COL),
            visions.toMutableSet()
        )
        division.register(publisher)
        return division
    }

    override fun save(division: PrecogDivision) {
        val updatedRows = dsl.update(PRECOG_TABLE)
            .set(STATS_COL, division.totalCrimesPrevented)
            .set(VERSION_COL, VERSION_COL.plus(1))
            .where(ID_COL.eq(division.id))
            .and(VERSION_COL.eq(division.version))
            .execute()

        if (updatedRows == 0) {
            throw ch.ejpd.example.precrime.domain.OptimisticLockingException("PrecogDivision with ID ${division.id} was updated or deleted by another transaction")
        }

        division.version++

        // Delete all and re-insert for simplicity
        dsl.deleteFrom(VISION_TABLE)
            .where(PRECOG_ID_COL.eq(division.id))
            .execute()

        val inserts = division.visions.map { vision ->
            dsl.insertInto(VISION_TABLE)
                .set(VISION_ID_COL, vision.id)
                .set(PRECOG_ID_COL, division.id)
                .set(PERPETRATOR_COL, vision.perpetrator)
                .set(CRIME_TYPE_COL, vision.crimeType)
                .set(FORESEEN_AT_COL, vision.foreseenAt)
        }
        if (inserts.isNotEmpty()) {
            dsl.batch(inserts).execute()
        }
    }

    override fun findSingleton(): PrecogDivision = findById(SINGLETON_ID)!!
}
