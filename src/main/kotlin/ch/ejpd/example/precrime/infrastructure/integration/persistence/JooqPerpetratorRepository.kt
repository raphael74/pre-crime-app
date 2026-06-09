package ch.ejpd.example.precrime.infrastructure.integration.persistence

import ch.ejpd.example.precrime.domain.perpetrator.Perpetrator
import ch.ejpd.example.precrime.domain.perpetrator.PerpetratorId
import ch.ejpd.example.precrime.domain.perpetrator.PerpetratorRepository
import ch.ejpd.example.precrime.infrastructure.integration.persistence.jooq.tables.references.PERPETRATOR
import org.jooq.DSLContext
import org.jooq.Record
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class JooqPerpetratorRepository(
    private val dsl: DSLContext
) : PerpetratorRepository {

    override fun findById(id: PerpetratorId): Perpetrator? {
        return dsl.selectFrom(PERPETRATOR)
            .where(PERPETRATOR.ID.eq(id))
            .fetchOne()
            ?.toPerpetrator()
    }

    override fun findByIds(ids: Collection<PerpetratorId>): List<Perpetrator> {
        return dsl.selectFrom(PERPETRATOR)
            .where(PERPETRATOR.ID.`in`(ids))
            .fetch()
            .map { it.toPerpetrator() }
    }

    override fun findByFirstAndLastName(firstName: String, lastName: String): Perpetrator? {
        return dsl.selectFrom(PERPETRATOR)
            .where(PERPETRATOR.FIRST_NAME.eq(firstName))
            .and(PERPETRATOR.LAST_NAME.eq(lastName))
            .fetchOne()
            ?.toPerpetrator()
    }

    @Transactional(propagation = Propagation.MANDATORY)
    override fun save(perpetrator: Perpetrator) {
        val exists = dsl.fetchExists(
            dsl.selectOne()
                .from(PERPETRATOR)
                .where(PERPETRATOR.ID.eq(perpetrator.id))
        )
        if (exists) {
            val updatedRows = dsl.update(PERPETRATOR)
                .set(PERPETRATOR.FIRST_NAME, perpetrator.firstName)
                .set(PERPETRATOR.LAST_NAME, perpetrator.lastName)
                .set(PERPETRATOR.VERSION, perpetrator.version.increment())
                .where(PERPETRATOR.ID.eq(perpetrator.id))
                .and(PERPETRATOR.VERSION.eq(perpetrator.version))
                .execute()

            if (updatedRows == 0) {
                throw OptimisticLockingException("Perpetrator with ID ${perpetrator.id} was updated or deleted by another transaction")
            }
            perpetrator.version = perpetrator.version.increment()
        } else {
            dsl.insertInto(PERPETRATOR)
                .set(PERPETRATOR.ID, perpetrator.id)
                .set(PERPETRATOR.VERSION, perpetrator.version)
                .set(PERPETRATOR.FIRST_NAME, perpetrator.firstName)
                .set(PERPETRATOR.LAST_NAME, perpetrator.lastName)
                .execute()
        }
    }

    override fun findAll(): List<Perpetrator> {
        return dsl.selectFrom(PERPETRATOR)
            .orderBy(PERPETRATOR.FIRST_NAME, PERPETRATOR.LAST_NAME)
            .fetch()
            .map { it.toPerpetrator() }
    }

    private fun Record.toPerpetrator() = Perpetrator(
        id = get(PERPETRATOR.ID)!!,
        version = get(PERPETRATOR.VERSION)!!,
        firstName = get(PERPETRATOR.FIRST_NAME)!!,
        lastName = get(PERPETRATOR.LAST_NAME)!!
    )
}
