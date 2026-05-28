package ch.ejpd.example.precrime.infrastructure.integration.persistence

import ch.ejpd.example.precrime.domain.vision.Perpetrator
import ch.ejpd.example.precrime.domain.vision.Vision
import ch.ejpd.example.precrime.domain.vision.VisionId
import ch.ejpd.example.precrime.domain.vision.VisionRepository
import ch.ejpd.example.precrime.infrastructure.integration.persistence.jooq.tables.references.VISION
import org.jooq.DSLContext
import org.jooq.Record
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class JooqVisionRepository(
    private val dsl: DSLContext
) : VisionRepository {

    override fun findById(id: VisionId): Vision? {
        return dsl.select(
            VISION.ID,
            VISION.VERSION,
            VISION.FIRST_NAME,
            VISION.LAST_NAME,
            VISION.CRIME_TYPE,
            VISION.FORESEEN_AT
        )
            .from(VISION)
            .where(VISION.ID.eq(id))
            .fetchOne()
            ?.toVision()
    }

    @Transactional(propagation = Propagation.MANDATORY)
    override fun create(vision: Vision) {
        dsl.insertInto(VISION)
            .set(VISION.ID, vision.id)
            .set(VISION.FIRST_NAME, vision.perpetrator.firstName)
            .set(VISION.LAST_NAME, vision.perpetrator.lastName)
            .set(VISION.CRIME_TYPE, vision.crimeType)
            .set(VISION.FORESEEN_AT, vision.foreseenAt)
            .set(VISION.VERSION, vision.version)
            .execute()
    }

    @Transactional(propagation = Propagation.MANDATORY)
    override fun update(vision: Vision) {
        val updatedRows = dsl.update(VISION)
            .set(VISION.FIRST_NAME, vision.perpetrator.firstName)
            .set(VISION.LAST_NAME, vision.perpetrator.lastName)
            .set(VISION.CRIME_TYPE, vision.crimeType)
            .set(VISION.FORESEEN_AT, vision.foreseenAt)
            .set(VISION.VERSION, vision.version.increment())
            .where(VISION.ID.eq(vision.id))
            .and(VISION.VERSION.eq(vision.version))
            .execute()

        if (updatedRows == 0) {
            throw OptimisticLockingException("Vision with ID ${vision.id} was updated or deleted by another transaction")
        }

        vision.version = vision.version.increment()
    }

    private fun Record.toVision() = Vision(
        get(VISION.ID)!!,
        get(VISION.VERSION)!!,
        Perpetrator(get(VISION.FIRST_NAME)!!, get(VISION.LAST_NAME)!!),
        get(VISION.CRIME_TYPE)!!,
        get(VISION.FORESEEN_AT)!!
    )

}
