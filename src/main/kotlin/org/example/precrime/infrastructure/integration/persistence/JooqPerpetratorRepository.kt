package org.example.precrime.infrastructure.integration.persistence

import org.example.precrime.domain.perpetrator.Perpetrator
import org.example.precrime.domain.perpetrator.PerpetratorId
import org.example.precrime.domain.perpetrator.PerpetratorRepository
import org.example.precrime.infrastructure.integration.persistence.jooq.tables.references.PERPETRATOR
import org.jooq.DSLContext
import org.jooq.Record
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
@CacheConfig(cacheNames = ["perpetrators"])
class JooqPerpetratorRepository(
    private val dsl: DSLContext
) : PerpetratorRepository {

    @Cacheable
    override fun findById(id: PerpetratorId): Perpetrator? {
        return dsl.selectFrom(PERPETRATOR)
            .where(PERPETRATOR.ID.eq(id))
            .fetchOne()
            ?.toPerpetrator()
    }

    @Cacheable
    override fun findByFirstAndLastName(firstName: String, lastName: String): Perpetrator? {
        return dsl.selectFrom(PERPETRATOR)
            .where(PERPETRATOR.FIRST_NAME.eq(firstName))
            .and(PERPETRATOR.LAST_NAME.eq(lastName))
            .fetchOne()
            ?.toPerpetrator()
    }

    @Transactional(propagation = Propagation.MANDATORY)
    @CacheEvict(allEntries = true)
    override fun create(perpetrator: Perpetrator) {
        dsl.insertInto(PERPETRATOR)
            .set(PERPETRATOR.ID, perpetrator.id)
            .set(PERPETRATOR.VERSION, perpetrator.version)
            .set(PERPETRATOR.FIRST_NAME, perpetrator.firstName)
            .set(PERPETRATOR.LAST_NAME, perpetrator.lastName)
            .execute()
    }

    @Transactional(propagation = Propagation.MANDATORY)
    @CacheEvict(key = "#perpetrator.id")
    override fun update(perpetrator: Perpetrator) {
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
    }

    @Cacheable
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
