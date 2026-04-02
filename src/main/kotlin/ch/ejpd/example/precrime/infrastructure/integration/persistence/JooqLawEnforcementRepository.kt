package ch.ejpd.example.precrime.infrastructure.integration.persistence

import ch.ejpd.example.precrime.domain.DomainEventPublisher
import ch.ejpd.example.precrime.domain.enforcement.EnforcementUnitId
import ch.ejpd.example.precrime.domain.enforcement.LawEnforcementRepository
import ch.ejpd.example.precrime.domain.enforcement.LawEnforcementUnit
import org.jooq.DSLContext
import org.jooq.impl.DSL.field
import org.jooq.impl.DSL.table
import org.springframework.stereotype.Component
import java.util.*

@Component
class JooqLawEnforcementRepository(
    private val dsl: DSLContext,
    private val publisher: DomainEventPublisher
) : LawEnforcementRepository {

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

        val unit = LawEnforcementUnit(EnforcementUnitId(record.get(ID_COL)), record.get(NAME_COL))
        unit.register(publisher)
        return unit
    }

    override fun save(unit: LawEnforcementUnit) {
        dsl.update(UNIT_TABLE)
            .set(NAME_COL, unit.unitName)
            .where(ID_COL.eq(unit.id.value))
            .execute()
    }

    override fun findSingleton(): LawEnforcementUnit = findById(EnforcementUnitId(SINGLETON_ID))!!
}
