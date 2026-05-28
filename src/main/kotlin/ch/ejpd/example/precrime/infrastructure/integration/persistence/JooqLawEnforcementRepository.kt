package ch.ejpd.example.precrime.infrastructure.integration.persistence

import ch.ejpd.example.precrime.domain.enforcement.*
import ch.ejpd.example.precrime.domain.precog.Perpetrator
import ch.ejpd.example.precrime.infrastructure.integration.persistence.jooq.tables.references.LAW_ENFORCEMENT_UNIT
import ch.ejpd.example.precrime.infrastructure.integration.persistence.jooq.tables.references.PRE_ARREST
import org.jooq.DSLContext
import org.jooq.Record
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Component
class JooqLawEnforcementRepository(
    private val dsl: DSLContext
) : LawEnforcementRepository {

    private val SINGLETON_ID = EnforcementUnitId(UUID.fromString("00000000-0000-0000-0000-000000000002"))

    override fun findById(id: EnforcementUnitId): LawEnforcementUnit? {
        val record = dsl.select(LAW_ENFORCEMENT_UNIT.ID, LAW_ENFORCEMENT_UNIT.UNIT_NAME, LAW_ENFORCEMENT_UNIT.VERSION)
            .from(LAW_ENFORCEMENT_UNIT)
            .where(LAW_ENFORCEMENT_UNIT.ID.eq(id))
            .fetchOne() ?: return null

        val preArrests = dsl.select(
            PRE_ARREST.ID,
            PRE_ARREST.VISION_ID,
            PRE_ARREST.FIRST_NAME,
            PRE_ARREST.PERPETRATOR,
            PRE_ARREST.STATUS
        )
            .from(PRE_ARREST)
            .where(PRE_ARREST.ENFORCEMENT_UNIT_ID.eq(id))
            .fetch()
            .map { it.toPreArrest() }

        return record.toLawEnforcementUnit(preArrests.toSet())
    }

    @Transactional(propagation = Propagation.MANDATORY)
    override fun update(unit: LawEnforcementUnit) {
        val updatedRows = dsl.update(LAW_ENFORCEMENT_UNIT)
            .set(LAW_ENFORCEMENT_UNIT.UNIT_NAME, unit.unitName.value)
            .set(LAW_ENFORCEMENT_UNIT.VERSION, unit.version.increment())
            .where(LAW_ENFORCEMENT_UNIT.ID.eq(unit.id))
            .and(LAW_ENFORCEMENT_UNIT.VERSION.eq(unit.version))
            .execute()

        if (updatedRows == 0) {
            throw OptimisticLockingException("LawEnforcementUnit with ID ${unit.id} was updated or deleted by another transaction")
        }

        unit.version = unit.version.increment()

        // Optimized save: only insert pre-arrests that are not already in the DB
        unit.preArrests.forEach { arrest ->
            val exists = dsl.fetchExists(
                dsl.selectOne()
                    .from(PRE_ARREST)
                    .where(PRE_ARREST.ID.eq(arrest.id))
            )
            if (!exists) {
                dsl.insertInto(PRE_ARREST)
                    .set(PRE_ARREST.ID, arrest.id)
                    .set(PRE_ARREST.ENFORCEMENT_UNIT_ID, unit.id)
                    .set(PRE_ARREST.VISION_ID, arrest.visionId)
                    .set(PRE_ARREST.FIRST_NAME, arrest.perpetrator.firstName)
                    .set(PRE_ARREST.PERPETRATOR, arrest.perpetrator.lastName)
                    .set(PRE_ARREST.STATUS, arrest.status)
                    .execute()
            }
        }

    }

    override fun findSingleton(): LawEnforcementUnit = findById(SINGLETON_ID)!!

    private fun Record.toPreArrest() = PreArrest(
        get(PRE_ARREST.ID)!!,
        get(PRE_ARREST.VISION_ID)!!,
        Perpetrator(get(PRE_ARREST.FIRST_NAME)!!, get(PRE_ARREST.PERPETRATOR)!!),
        get(PRE_ARREST.STATUS)!!
    )

    private fun Record.toLawEnforcementUnit(preArrests: Set<PreArrest>) = LawEnforcementUnit(
        get(LAW_ENFORCEMENT_UNIT.ID)!!,
        get(LAW_ENFORCEMENT_UNIT.VERSION)!!,
        UnitName(get(LAW_ENFORCEMENT_UNIT.UNIT_NAME)!!),
        preArrests
    )
}
