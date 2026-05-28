package ch.ejpd.example.precrime.infrastructure.integration.persistence

import ch.ejpd.example.precrime.domain.precog.*
import ch.ejpd.example.precrime.infrastructure.integration.persistence.jooq.tables.references.PRECOG_DIVISION
import ch.ejpd.example.precrime.infrastructure.integration.persistence.jooq.tables.references.VISION
import org.jooq.DSLContext
import org.jooq.Record
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Component
class JooqPrecogDivisionRepository(
    private val dsl: DSLContext
) : PrecogDivisionRepository {

    private val SINGLETON_ID = PrecogDivisionId(UUID.fromString("00000000-0000-0000-0000-000000000001"))

    override fun findById(id: PrecogDivisionId): PrecogDivision? {
        val record = dsl.select(PRECOG_DIVISION.ID, PRECOG_DIVISION.TOTAL_CRIMES_PREVENTED, PRECOG_DIVISION.VERSION)
            .from(PRECOG_DIVISION)
            .where(PRECOG_DIVISION.ID.eq(id))
            .fetchOne() ?: return null

        val visions =
            dsl.select(VISION.ID, VISION.FIRST_NAME, VISION.PERPETRATOR, VISION.CRIME_TYPE, VISION.FORESEEN_AT)
                .from(VISION)
                .where(VISION.PRECOG_DIVISION_ID.eq(id))
                .fetch()
                .map { it.toVision() }

        return record.toPrecogDivision(visions.toSet())
    }

    @Transactional(propagation = Propagation.MANDATORY)
    override fun update(division: PrecogDivision) {
        val updatedRows = dsl.update(PRECOG_DIVISION)
            .set(PRECOG_DIVISION.TOTAL_CRIMES_PREVENTED, division.totalCrimesPrevented)
            .set(PRECOG_DIVISION.VERSION, division.version.increment())
            .where(PRECOG_DIVISION.ID.eq(division.id))
            .and(PRECOG_DIVISION.VERSION.eq(division.version))
            .execute()

        if (updatedRows == 0) {
            throw OptimisticLockingException("PrecogDivision with ID ${division.id} was updated or deleted by another transaction")
        }

        division.version = division.version.increment()

        // Optimized save: only insert visions that are not already in the DB
        division.visions.forEach { vision ->
            val exists = dsl.fetchExists(
                dsl.selectOne()
                    .from(VISION)
                    .where(VISION.ID.eq(vision.id))
            )
            if (!exists) {
                dsl.insertInto(VISION)
                    .set(VISION.ID, vision.id)
                    .set(VISION.PRECOG_DIVISION_ID, division.id)
                    .set(VISION.FIRST_NAME, vision.perpetrator.firstName)
                    .set(VISION.PERPETRATOR, vision.perpetrator.lastName)
                    .set(VISION.CRIME_TYPE, vision.crimeType)
                    .set(VISION.FORESEEN_AT, vision.foreseenAt)
                    .execute()
            }
        }

    }

    override fun findSingleton(): PrecogDivision = findById(SINGLETON_ID)!!

    private fun Record.toVision() = Vision(
        get(VISION.ID)!!,
        Perpetrator(get(VISION.FIRST_NAME)!!, get(VISION.PERPETRATOR)!!),
        get(VISION.CRIME_TYPE)!!,
        get(VISION.FORESEEN_AT)!!
    )

    private fun Record.toPrecogDivision(visions: Set<Vision>) = PrecogDivision(
        get(PRECOG_DIVISION.ID)!!,
        get(PRECOG_DIVISION.VERSION)!!,
        get(PRECOG_DIVISION.TOTAL_CRIMES_PREVENTED)!!,
        visions
    )
}
