package ch.ejpd.example.precrime.infrastructure.integration.persistence

import ch.ejpd.example.precrime.domain.precog.PrecogDivision
import ch.ejpd.example.precrime.domain.precog.PrecogDivisionId
import ch.ejpd.example.precrime.domain.precog.PrecogDivisionRepository
import org.jooq.DSLContext
import org.jooq.impl.DSL.field
import org.jooq.impl.DSL.table
import org.springframework.stereotype.Component
import java.util.*

@Component
class JooqPrecogDivisionRepository(private val dsl: DSLContext) : PrecogDivisionRepository {
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