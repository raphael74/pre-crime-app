package ch.ejpd.example.precrime.infrastructure.integration.persistence

import ch.ejpd.example.precrime.domain.apology.*
import ch.ejpd.example.precrime.domain.precog.Perpetrator
import ch.ejpd.example.precrime.infrastructure.integration.persistence.jooq.tables.references.PRE_APOLOGY
import org.jooq.DSLContext
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Component
class JooqPreApologyRepository(
    private val dsl: DSLContext
) : PreApologyRepository {

    override fun findById(id: PreApologyId): PreApology? {
        val r = dsl.selectFrom(PRE_APOLOGY)
            .where(PRE_APOLOGY.ID.eq(id))
            .fetchOne() ?: return null

        return PreApology(
            id = r.get(PRE_APOLOGY.ID)!!,
            visionId = r.get(PRE_APOLOGY.VISION_ID)!!,
            perpetrator = Perpetrator(r.get(PRE_APOLOGY.PERPETRATOR)!!),
            compensation = Compensation(
                baseAmount = r.get(PRE_APOLOGY.BASE_AMOUNT)!!.toDouble(),
                jetpackFuelDeduction = r.get(PRE_APOLOGY.JETPACK_FUEL_DEDUCTION)!!.toDouble(),
                haloRentalFee = r.get(PRE_APOLOGY.HALO_RENTAL_FEE)!!.toDouble(),
                netPayout = r.get(PRE_APOLOGY.NET_PAYOUT)!!.toDouble()
            ),
            apologyLetter = ApologyLetter(r.get(PRE_APOLOGY.APOLOGY_TEXT)!!)
        )
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
                .set(PRE_APOLOGY.PERPETRATOR, apology.perpetrator.name)
                .set(PRE_APOLOGY.BASE_AMOUNT, BigDecimal.valueOf(apology.compensation.baseAmount))
                .set(PRE_APOLOGY.JETPACK_FUEL_DEDUCTION, BigDecimal.valueOf(apology.compensation.jetpackFuelDeduction))
                .set(PRE_APOLOGY.HALO_RENTAL_FEE, BigDecimal.valueOf(apology.compensation.haloRentalFee))
                .set(PRE_APOLOGY.NET_PAYOUT, BigDecimal.valueOf(apology.compensation.netPayout))
                .set(PRE_APOLOGY.APOLOGY_TEXT, apology.apologyLetter.text)
                .execute()
        }
    }

    override fun findAll(): List<PreApology> {
        return dsl.selectFrom(PRE_APOLOGY)
            .fetch()
            .map { r ->
                PreApology(
                    id = r.get(PRE_APOLOGY.ID)!!,
                    visionId = r.get(PRE_APOLOGY.VISION_ID)!!,
                    perpetrator = Perpetrator(r.get(PRE_APOLOGY.PERPETRATOR)!!),
                    compensation = Compensation(
                        baseAmount = r.get(PRE_APOLOGY.BASE_AMOUNT)!!.toDouble(),
                        jetpackFuelDeduction = r.get(PRE_APOLOGY.JETPACK_FUEL_DEDUCTION)!!.toDouble(),
                        haloRentalFee = r.get(PRE_APOLOGY.HALO_RENTAL_FEE)!!.toDouble(),
                        netPayout = r.get(PRE_APOLOGY.NET_PAYOUT)!!.toDouble()
                    ),
                    apologyLetter = ApologyLetter(r.get(PRE_APOLOGY.APOLOGY_TEXT)!!)
                )
            }
    }
}
