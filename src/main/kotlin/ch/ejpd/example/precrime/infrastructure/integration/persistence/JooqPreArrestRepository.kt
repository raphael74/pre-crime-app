package ch.ejpd.example.precrime.infrastructure.integration.persistence

import ch.ejpd.example.precrime.domain.prearrest.*
import ch.ejpd.example.precrime.domain.vision.Perpetrator
import ch.ejpd.example.precrime.infrastructure.integration.persistence.jooq.tables.references.PRE_ARREST
import org.jooq.DSLContext
import org.jooq.Record
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class JooqPreArrestRepository(
    private val dsl: DSLContext
) : PreArrestRepository {

    override fun findById(id: PreArrestId): PreArrest? {
        val record = dsl.select(
            PRE_ARREST.ID,
            PRE_ARREST.VERSION,
            PRE_ARREST.VISION_ID,
            PRE_ARREST.FIRST_NAME,
            PRE_ARREST.LAST_NAME,
            PRE_ARREST.STATUS
        )
            .from(PRE_ARREST)
            .where(PRE_ARREST.ID.eq(id))
            .fetchOne() ?: return null

        return record.toPreArrest()
    }

    @Transactional(propagation = Propagation.MANDATORY)
    override fun save(preArrest: PreArrest) {
        val exists = dsl.fetchExists(
            dsl.selectOne()
                .from(PRE_ARREST)
                .where(PRE_ARREST.ID.eq(preArrest.id))
        )
        if (exists) {
            val updatedRows = dsl.update(PRE_ARREST)
                .set(PRE_ARREST.STATUS, preArrest.status)
                .set(PRE_ARREST.VERSION, preArrest.version.increment())
                .where(PRE_ARREST.ID.eq(preArrest.id))
                .and(PRE_ARREST.VERSION.eq(preArrest.version))
                .execute()

            if (updatedRows == 0) {
                throw OptimisticLockingException("PreArrest with ID ${preArrest.id} was updated or deleted by another transaction")
            }
            preArrest.version = preArrest.version.increment()
        } else {
            dsl.insertInto(PRE_ARREST)
                .set(PRE_ARREST.ID, preArrest.id)
                .set(PRE_ARREST.VERSION, preArrest.version)
                .set(PRE_ARREST.VISION_ID, preArrest.visionId)
                .set(PRE_ARREST.FIRST_NAME, preArrest.perpetrator.firstName)
                .set(PRE_ARREST.LAST_NAME, preArrest.perpetrator.lastName)
                .set(PRE_ARREST.STATUS, preArrest.status)
                .execute()
        }
    }

    override fun findAll(): List<PreArrest> {
        return dsl.select(
            PRE_ARREST.ID,
            PRE_ARREST.VERSION,
            PRE_ARREST.VISION_ID,
            PRE_ARREST.FIRST_NAME,
            PRE_ARREST.LAST_NAME,
            PRE_ARREST.STATUS
        )
            .from(PRE_ARREST)
            .fetch()
            .map { it.toPreArrest() }
    }

    private fun Record.toPreArrest() = PreArrest(
        id = get(PRE_ARREST.ID)!!,
        version = get(PRE_ARREST.VERSION)!!,
        visionId = get(PRE_ARREST.VISION_ID)!!,
        perpetrator = Perpetrator(get(PRE_ARREST.FIRST_NAME)!!, get(PRE_ARREST.LAST_NAME)!!),
        status = get(PRE_ARREST.STATUS)!!
    )
}
