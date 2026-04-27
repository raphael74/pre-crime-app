package ch.ejpd.example.precrime.infrastructure.integration.persistence

import ch.ejpd.example.precrime.domain.DomainEventPublisher
import ch.ejpd.example.precrime.domain.enforcement.*
import ch.ejpd.example.precrime.domain.precog.VisionId
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
    private val ID_COL = uuidField("id", EnforcementUnitId::class.java, ::EnforcementUnitId, EnforcementUnitId::value)
    private val NAME_COL = field("unit_name", String::class.java)
    private val VERSION_COL = field("version", Long::class.java)

    private val ARREST_ID_COL = uuidField("id", PreArrestId::class.java, ::PreArrestId, PreArrestId::value)
    private val UNIT_ID_COL =
        uuidField("enforcement_unit_id", EnforcementUnitId::class.java, ::EnforcementUnitId, EnforcementUnitId::value)
    private val VISION_ID_COL = uuidField("vision_id", VisionId::class.java, ::VisionId, VisionId::value)
    private val PERPETRATOR_COL = field("perpetrator", String::class.java)
    private val STATUS_COL = field("status", String::class.java)

    private val SINGLETON_ID = EnforcementUnitId(UUID.fromString("00000000-0000-0000-0000-000000000002"))

    override fun findById(id: EnforcementUnitId): LawEnforcementUnit? {
        val record = dsl.select(ID_COL, NAME_COL, VERSION_COL)
            .from(UNIT_TABLE)
            .where(ID_COL.eq(id))
            .fetchOne() ?: return null

        val preArrests = dsl.select(ARREST_ID_COL, VISION_ID_COL, PERPETRATOR_COL, STATUS_COL)
            .from(ARREST_TABLE)
            .where(UNIT_ID_COL.eq(id))
            .fetch()
            .map { r ->
                PreArrest(r.get(ARREST_ID_COL), r.get(VISION_ID_COL), r.get(PERPETRATOR_COL), r.get(STATUS_COL))
            }

        val unit = LawEnforcementUnit(
            record.get(ID_COL),
            record.get(VERSION_COL),
            record.get(NAME_COL),
            preArrests.toMutableSet()
        )
        unit.register(publisher)
        return unit
    }

    override fun save(unit: LawEnforcementUnit) {
        val updatedRows = dsl.update(UNIT_TABLE)
            .set(NAME_COL, unit.unitName)
            .set(VERSION_COL, VERSION_COL.plus(1))
            .where(ID_COL.eq(unit.id))
            .and(VERSION_COL.eq(unit.version))
            .execute()

        if (updatedRows == 0) {
            throw ch.ejpd.example.precrime.domain.OptimisticLockingException("LawEnforcementUnit with ID ${unit.id} was updated or deleted by another transaction")
        }

        unit.version++

        // Delete all and re-insert for simplicity in this demo
        dsl.deleteFrom(ARREST_TABLE)
            .where(UNIT_ID_COL.eq(unit.id))
            .execute()

        val inserts = unit.preArrests.map { arrest ->
            dsl.insertInto(ARREST_TABLE)
                .set(ARREST_ID_COL, arrest.id)
                .set(UNIT_ID_COL, unit.id)
                .set(VISION_ID_COL, arrest.visionId)
                .set(PERPETRATOR_COL, arrest.perpetrator)
                .set(STATUS_COL, arrest.status)
        }
        if (inserts.isNotEmpty()) {
            dsl.batch(inserts).execute()
        }
    }

    override fun findSingleton(): LawEnforcementUnit = findById(SINGLETON_ID)!!
}
