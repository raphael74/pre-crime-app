package ch.ejpd.example.precrime.domain.prearrest

import org.jmolecules.ddd.annotation.Repository

@Repository
interface PreArrestRepository {
    fun findById(id: PreArrestId): PreArrest?
    fun save(preArrest: PreArrest)
    fun findAll(): List<PreArrest>
    fun findAllArrested(): List<PreArrest>
    fun findAllPending(): List<PreArrest>
}