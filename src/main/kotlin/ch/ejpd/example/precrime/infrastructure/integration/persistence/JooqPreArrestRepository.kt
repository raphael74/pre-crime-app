package ch.ejpd.example.precrime.infrastructure.integration.persistence

import ch.ejpd.example.precrime.domain.prearrest.PreArrest
import ch.ejpd.example.precrime.domain.prearrest.PreArrestId
import ch.ejpd.example.precrime.domain.prearrest.PreArrestRepository
import ch.ejpd.example.precrime.domain.prearrest.PreArrestStatus
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
            PRE_ARREST.PERPETRATOR_ID,
            PRE_ARREST.PRE_ARREST_ISSUED_DATE,
            PRE_ARREST.PRE_ARREST_DATE,
            PRE_ARREST.STATUS
        )
            .from(PRE_ARREST)
            .where(PRE_ARREST.ID.eq(id))
            .fetchOne() ?: return null

        return record.toPreArrest()
    }

    @Transactional(propagation = Propagation.MANDATORY)
    override fun create(preArrest: PreArrest) {
        val exists = dsl.fetchExists(
            dsl.selectOne()
                .from(PRE_ARREST)
                .where(PRE_ARREST.ID.eq(preArrest.id))
        )
        if (exists) {
            val updatedRows = dsl.update(PRE_ARREST)
                .set(PRE_ARREST.STATUS, preArrest.status)
                .set(PRE_ARREST.VERSION, preArrest.version.increment())
                .set(PRE_ARREST.PRE_ARREST_DATE, preArrest.preArrestDate)
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
                .set(PRE_ARREST.PERPETRATOR_ID, preArrest.perpetratorId)
                .set(PRE_ARREST.PRE_ARREST_ISSUED_DATE, preArrest.preArrestIssuedDate)
                .set(PRE_ARREST.PRE_ARREST_DATE, preArrest.preArrestDate)
                .set(PRE_ARREST.STATUS, preArrest.status)
                .execute()
        }
    }

    override fun findAll(): List<PreArrest> {
        return dsl.select(
            PRE_ARREST.ID,
            PRE_ARREST.VERSION,
            PRE_ARREST.VISION_ID,
            PRE_ARREST.PERPETRATOR_ID,
            PRE_ARREST.PRE_ARREST_ISSUED_DATE,
            PRE_ARREST.PRE_ARREST_DATE,
            PRE_ARREST.STATUS
        )
            .from(PRE_ARREST)
            .orderBy(PRE_ARREST.PRE_ARREST_ISSUED_DATE.desc())
            .fetch()
            .map { it.toPreArrest() }
    }

    override fun findAllPending(): List<PreArrest> {
        return dsl.select(
            PRE_ARREST.ID,
            PRE_ARREST.VERSION,
            PRE_ARREST.VISION_ID,
            PRE_ARREST.PERPETRATOR_ID,
            PRE_ARREST.PRE_ARREST_ISSUED_DATE,
            PRE_ARREST.PRE_ARREST_DATE,
            PRE_ARREST.STATUS
        )
            .from(PRE_ARREST)
            .where(PRE_ARREST.STATUS.eq(PreArrestStatus.PENDING))
            .orderBy(PRE_ARREST.PRE_ARREST_ISSUED_DATE.desc())
            .fetch()
            .map { it.toPreArrest() }
    }

    override fun findAllArrested(): List<PreArrest> {
        return dsl.select(
            PRE_ARREST.ID,
            PRE_ARREST.VERSION,
            PRE_ARREST.VISION_ID,
            PRE_ARREST.PERPETRATOR_ID,
            PRE_ARREST.PRE_ARREST_ISSUED_DATE,
            PRE_ARREST.PRE_ARREST_DATE,
            PRE_ARREST.STATUS
        )
            .from(PRE_ARREST)
            .where(PRE_ARREST.STATUS.eq(PreArrestStatus.ARRESTED_BEFORE_CRIME))
            .orderBy(PRE_ARREST.PRE_ARREST_DATE.desc())
            .fetch()
            .map { it.toPreArrest() }
    }

    private fun Record.toPreArrest() = PreArrest(
        id = get(PRE_ARREST.ID)!!,
        version = get(PRE_ARREST.VERSION)!!,
        visionId = get(PRE_ARREST.VISION_ID)!!,
        perpetratorId = get(PRE_ARREST.PERPETRATOR_ID)!!,
        preArrestIssuedDate = get(PRE_ARREST.PRE_ARREST_ISSUED_DATE)!!,
        preArrestDate = get(PRE_ARREST.PRE_ARREST_DATE),
        status = get(PRE_ARREST.STATUS)!!
    )
}
