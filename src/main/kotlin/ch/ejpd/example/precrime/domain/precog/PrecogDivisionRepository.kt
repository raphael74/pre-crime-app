package ch.ejpd.example.precrime.domain.precog

import org.jmolecules.ddd.annotation.Repository

@Repository
interface PrecogDivisionRepository {
    fun findById(id: PrecogDivisionId): PrecogDivision?
    fun save(division: PrecogDivision)
    fun findSingleton(): PrecogDivision
}
