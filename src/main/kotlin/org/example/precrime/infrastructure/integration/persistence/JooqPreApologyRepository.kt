package org.example.precrime.infrastructure.integration.persistence

import org.example.precrime.domain.preapology.*
import org.example.precrime.infrastructure.integration.persistence.jooq.tables.references.PRE_APOLOGY
import org.jooq.DSLContext
import org.jooq.Record
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Component
class JooqPreApologyRepository(
    private val dsl: DSLContext
) : PreApologyRepository {

    override fun findById(id: PreApologyId): PreApology? {
        return dsl.selectFrom(PRE_APOLOGY)
            .where(PRE_APOLOGY.ID.eq(id))
            .fetchOne()
            ?.toPreApology()
    }

    @Transactional(propagation = Propagation.MANDATORY)
    override fun create(apology: PreApology) {
        dsl.insertInto(PRE_APOLOGY)
            .set(PRE_APOLOGY.ID, apology.id)
            .set(PRE_APOLOGY.PRE_ARREST_ID, apology.preArrestId)
            .set(PRE_APOLOGY.PERPETRATOR_ID, apology.perpetratorId)
            .set(PRE_APOLOGY.BASE_AMOUNT, BigDecimal.valueOf(apology.compensation.baseAmount))
            .set(PRE_APOLOGY.JETPACK_FUEL_DEDUCTION, BigDecimal.valueOf(apology.compensation.jetpackFuelDeduction))
            .set(PRE_APOLOGY.HALO_RENTAL_FEE, BigDecimal.valueOf(apology.compensation.haloRentalFee))
            .set(PRE_APOLOGY.NET_PAYOUT, BigDecimal.valueOf(apology.compensation.netPayout))
            .set(PRE_APOLOGY.APOLOGY_TEXT, apology.apologyLetter.text)
            .set(PRE_APOLOGY.CREATED_AT, apology.createdAt)
            .execute()
    }

    override fun findAll(): List<PreApology> {
        return dsl.selectFrom(PRE_APOLOGY)
            .orderBy(PRE_APOLOGY.CREATED_AT.desc())
            .fetch()
            .map { it.toPreApology() }
    }

    private fun Record.toPreApology() = PreApology(
        id = get(PRE_APOLOGY.ID)!!,
        preArrestId = get(PRE_APOLOGY.PRE_ARREST_ID)!!,
        perpetratorId = get(PRE_APOLOGY.PERPETRATOR_ID)!!,
        compensation = Compensation(
            baseAmount = get(PRE_APOLOGY.BASE_AMOUNT)!!.toDouble(),
            jetpackFuelDeduction = get(PRE_APOLOGY.JETPACK_FUEL_DEDUCTION)!!.toDouble(),
            haloRentalFee = get(PRE_APOLOGY.HALO_RENTAL_FEE)!!.toDouble(),
            netPayout = get(PRE_APOLOGY.NET_PAYOUT)!!.toDouble()
        ),
        apologyLetter = ApologyLetter(get(PRE_APOLOGY.APOLOGY_TEXT)!!),
        createdAt = get(PRE_APOLOGY.CREATED_AT)!!
    )
}
