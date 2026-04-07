package ch.ejpd.example.precrime.infrastructure.integration.persistence

import ch.ejpd.example.precrime.domain.DomainEventPublisher
import ch.ejpd.example.precrime.domain.precog.PrecogDivision
import ch.ejpd.example.precrime.domain.precog.PrecogDivisionId
import ch.ejpd.example.precrime.domain.precog.PrecogDivisionRepository
import org.jooq.DSLContext
import org.jooq.impl.DSL.field
import org.jooq.impl.DSL.table
import org.springframework.stereotype.Component
import java.util.*

@Component
class JooqPrecogDivisionRepository(
    private val dsl: DSLContext,
    private val publisher: DomainEventPublisher
) : PrecogDivisionRepository {
    private val PRECOG_TABLE = table("precog_division")
    private val ID_COL = uuidField("id", PrecogDivisionId::class.java, ::PrecogDivisionId, PrecogDivisionId::value)
    private val STATS_COL = field("total_crimes_prevented", Int::class.java)

    private val SINGLETON_ID = PrecogDivisionId(UUID.fromString("00000000-0000-0000-0000-000000000001"))

    override fun findById(id: PrecogDivisionId): PrecogDivision? {
        val record = dsl.select(ID_COL, STATS_COL)
            .from(PRECOG_TABLE)
            .where(ID_COL.eq(id))
            .fetchOne() ?: return null

        val division = PrecogDivision(record.get(ID_COL), record.get(STATS_COL))
        division.register(publisher)
        return division
    }

    override fun save(division: PrecogDivision) {
        dsl.update(PRECOG_TABLE)
            .set(STATS_COL, division.totalCrimesPrevented)
            .where(ID_COL.eq(division.id))
            .execute()
    }

    override fun findSingleton(): PrecogDivision = findById(SINGLETON_ID)!!
}