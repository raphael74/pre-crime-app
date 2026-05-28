package ch.ejpd.example.precrime.infrastructure.integration.persistence

import ch.ejpd.example.precrime.domain.apology.*
import ch.ejpd.example.precrime.domain.precog.Perpetrator
import ch.ejpd.example.precrime.infrastructure.integration.persistence.jooq.tables.references.PRE_APOLOGY
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
    override fun save(apology: PreApology) {
        val exists = dsl.fetchExists(
            dsl.selectOne()
                .from(PRE_APOLOGY)
                .where(PRE_APOLOGY.ID.eq(apology.id))
        )
        if (!exists) {
            dsl.insertInto(PRE_APOLOGY)
                .set(PRE_APOLOGY.ID, apology.id)
                .set(PRE_APOLOGY.VISION_ID, apology.visionId)
                .set(PRE_APOLOGY.FIRST_NAME, apology.perpetrator.firstName)
                .set(PRE_APOLOGY.LAST_NAME, apology.perpetrator.lastName)
                .set(PRE_APOLOGY.BASE_AMOUNT, BigDecimal.valueOf(apology.compensation.baseAmount))
                .set(PRE_APOLOGY.JETPACK_FUEL_DEDUCTION, BigDecimal.valueOf(apology.compensation.jetpackFuelDeduction))
                .set(PRE_APOLOGY.HALO_RENTAL_FEE, BigDecimal.valueOf(apology.compensation.haloRentalFee))
                .set(PRE_APOLOGY.NET_PAYOUT, BigDecimal.valueOf(apology.compensation.netPayout))
                .set(PRE_APOLOGY.APOLOGY_TEXT, apology.apologyLetter.text)
                .set(PRE_APOLOGY.CREATED_AT, apology.createdAt)
                .execute()
        }
    }

    override fun findAll(): List<PreApology> {
        return dsl.selectFrom(PRE_APOLOGY)
            .orderBy(PRE_APOLOGY.CREATED_AT.desc())
            .fetch()
            .map { it.toPreApology() }
    }

    private fun Record.toPreApology() = PreApology(
        id = get(PRE_APOLOGY.ID)!!,
        visionId = get(PRE_APOLOGY.VISION_ID)!!,
        perpetrator = Perpetrator(get(PRE_APOLOGY.FIRST_NAME)!!, get(PRE_APOLOGY.LAST_NAME)!!),
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
