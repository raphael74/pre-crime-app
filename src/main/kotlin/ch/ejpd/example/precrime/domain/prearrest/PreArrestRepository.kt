package ch.ejpd.example.precrime.domain.prearrest

import org.jmolecules.ddd.annotation.Repository

@Repository
interface PreArrestRepository {
    fun findById(id: PreArrestId): PreArrest?
    fun create(preArrest: PreArrest)
    fun update(preArrest: PreArrest)
    fun findAll(): List<PreArrest>
    fun findAllArrested(): List<PreArrest>
    fun findAllPending(): List<PreArrest>
}