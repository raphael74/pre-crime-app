package ch.ejpd.example.precrime.infrastructure.integration.persistence

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
            VISION.PERPETRATOR_ID,
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
            .set(VISION.PERPETRATOR_ID, vision.perpetratorId)
            .set(VISION.CRIME_TYPE, vision.crimeType)
            .set(VISION.FORESEEN_AT, vision.foreseenAt)
            .set(VISION.VERSION, vision.version)
            .execute()
    }

    @Transactional(propagation = Propagation.MANDATORY)
    override fun update(vision: Vision) {
        val updatedRows = dsl.update(VISION)
            .set(VISION.PERPETRATOR_ID, vision.perpetratorId)
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
        id = get(VISION.ID)!!,
        version = get(VISION.VERSION)!!,
        perpetratorId = get(VISION.PERPETRATOR_ID)!!,
        crimeType = get(VISION.CRIME_TYPE)!!,
        foreseenAt = get(VISION.FORESEEN_AT)!!
    )

}
