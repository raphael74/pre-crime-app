package ch.ejpd.example.precrime.infrastructure.integration.persistence

import ch.ejpd.example.precrime.domain.enforcement.EnforcementUnitId
import ch.ejpd.example.precrime.domain.enforcement.LawEnforcementRepository
import ch.ejpd.example.precrime.domain.enforcement.LawEnforcementUnit
import ch.ejpd.example.precrime.domain.precog.PrecogDivision
import ch.ejpd.example.precrime.domain.precog.PrecogDivisionId
import ch.ejpd.example.precrime.domain.precog.PrecogDivisionRepository
import org.jooq.DSLContext
import org.jooq.impl.DSL.field
import org.jooq.impl.DSL.table
import org.springframework.stereotype.Component
import java.util.*

@Component
class JooqPrecogRepository(private val dsl: DSLContext) : PrecogDivisionRepository {
    private val PRECOG_TABLE = table("precog_division")
    private val ID_COL = field("id", UUID::class.java)
    private val STATS_COL = field("total_crimes_prevented", Int::class.java)
    private val SINGLETON_ID = UUID.fromString("00000000-0000-0000-0000-000000000001")

    override fun findById(id: PrecogDivisionId): PrecogDivision? {
        val record = dsl.selectFrom(PRECOG_TABLE)
            .where(ID_COL.eq(id.value))
            .fetchOne() ?: return null

        return PrecogDivision(PrecogDivisionId(record.get(ID_COL)), record.get(STATS_COL))
    }

    override fun save(division: PrecogDivision) {
        dsl.update(PRECOG_TABLE)
            .set(STATS_COL, division.totalCrimesPrevented)
            .where(ID_COL.eq(division.id.value))
            .execute()
    }

    override fun findSingleton(): PrecogDivision = findById(PrecogDivisionId(SINGLETON_ID))!!
}

@Component
class JooqEnforcementRepository(private val dsl: DSLContext) : LawEnforcementRepository {
    private val UNIT_TABLE = table("law_enforcement_unit")
    private val ARREST_TABLE = table("pre_arrest")
    private val ID_COL = field("id", UUID::class.java)
    private val NAME_COL = field("unit_name", String::class.java)
    private val UNIT_ID_COL = field("enforcement_unit_id", UUID::class.java)
    private val VISION_ID_COL = field("vision_id", UUID::class.java)
    private val PERPETRATOR_COL = field("perpetrator", String::class.java)
    private val STATUS_COL = field("status", String::class.java)

    private val SINGLETON_ID = UUID.fromString("00000000-0000-0000-0000-000000000002")

    override fun findById(id: EnforcementUnitId): LawEnforcementUnit? {
        val record = dsl.selectFrom(UNIT_TABLE)
            .where(ID_COL.eq(id.value))
            .fetchOne() ?: return null

        // Note: LawEnforcementUnit currently handles pre-arrests in memory for the aggregate.
        // In a full DDD/jOOQ setup, we'd load the child collection here.
        return LawEnforcementUnit(EnforcementUnitId(record.get(ID_COL)), record.get(NAME_COL))
    }

    override fun save(unit: LawEnforcementUnit) {
        dsl.update(UNIT_TABLE)
            .set(NAME_COL, unit.unitName)
            .where(ID_COL.eq(unit.id.value))
            .execute()
    }

    override fun findSingleton(): LawEnforcementUnit = findById(EnforcementUnitId(SINGLETON_ID))!!
}
