package ch.ejpd.example.precrime.domain.enforcement

import org.jmolecules.ddd.annotation.Repository

@Repository
interface LawEnforcementRepository {
    fun findById(id: EnforcementUnitId): LawEnforcementUnit?
    fun save(unit: LawEnforcementUnit)
    fun findSingleton(): LawEnforcementUnit
}
