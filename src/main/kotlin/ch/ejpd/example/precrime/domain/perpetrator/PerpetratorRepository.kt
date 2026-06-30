package ch.ejpd.example.precrime.domain.perpetrator

import org.jmolecules.ddd.annotation.Repository

@Repository
interface PerpetratorRepository {
    fun findById(id: PerpetratorId): Perpetrator?
    fun findByFirstAndLastName(firstName: String, lastName: String): Perpetrator?
    fun create(perpetrator: Perpetrator)
    fun update(perpetrator: Perpetrator)
    fun findAll(): List<Perpetrator>
}
