package ch.ejpd.example.precrime.domain.vision

import org.jmolecules.ddd.annotation.Repository

@Repository
interface VisionRepository {
    fun findById(id: VisionId): Vision?
    fun create(vision: Vision)
    fun update(vision: Vision)
}