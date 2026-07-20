package org.example.precrime.infrastructure.integration.persistence

import org.example.precrime.IntegrationTest
import org.example.precrime.domain.perpetrator.Perpetrator
import org.example.precrime.domain.perpetrator.PerpetratorId
import org.example.precrime.domain.perpetrator.PerpetratorRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.transaction.annotation.Transactional

@IntegrationTest
@Transactional
class JooqPerpetratorRepositoryCachingTest(
    @Autowired private var perpetratorRepository: PerpetratorRepository,
    @Autowired private var cacheManager: CacheManager
) {

    @Test
    fun `should cache perpetrator findById`() {
        val id = PerpetratorId()
        val perpetrator = Perpetrator(id, firstName = "John", lastName = "Doe")
        perpetratorRepository.create(perpetrator)

        // Clear cache to start fresh
        cacheManager.getCache("perpetrators")?.clear()

        // First call - should go to DB
        val firstCall = perpetratorRepository.findById(id)
        assertNotNull(firstCall)

        // Verify it's in cache
        val cache = cacheManager.getCache("perpetrators")
        assertNotNull(cache)
        val cachedValue = cache?.get(id.value)?.get() as? Perpetrator
        assertNotNull(cachedValue)
        assertEquals(perpetrator.id, cachedValue?.id)

        // Second call - should come from cache
        val secondCall = perpetratorRepository.findById(id)
        assertEquals(firstCall, secondCall)
    }

    @Test
    fun `should evict cache on save`() {
        val id = PerpetratorId()
        val perpetrator = Perpetrator(id, firstName = "Jane", lastName = "Doe")
        perpetratorRepository.create(perpetrator)

        // Fill cache
        perpetratorRepository.findById(id)
        val cache = cacheManager.getCache("perpetrators")
        assertNotNull(cache?.get(id.value))

        // Update perpetrator
        val updatedPerpetrator = Perpetrator(id, version = perpetrator.version, firstName = "Jane", lastName = "Meier")
        perpetratorRepository.update(updatedPerpetrator)

        // Cache should be empty
        assertEquals(null, cache?.get(id.value))
    }
}
