package ch.ejpd.example.precrime.domain.perpetrator

import org.jmolecules.ddd.annotation.Repository

@Repository
interface PerpetratorRepository {
    fun findById(id: PerpetratorId): Perpetrator?
    fun findByIds(ids: Collection<PerpetratorId>): List<Perpetrator>
    fun findByFirstAndLastName(firstName: String, lastName: String): Perpetrator?
    fun save(perpetrator: Perpetrator)
    fun findAll(): List<Perpetrator>
}