package ch.ejpd.example.precrime.domain.preapology

import org.jmolecules.ddd.annotation.Repository

@Repository
interface PreApologyRepository {
    fun save(apology: PreApology)
    fun findById(id: PreApologyId): PreApology?
    fun findAll(): List<PreApology>
}