package ch.ejpd.example.precrime

import ch.ejpd.example.precrime.domain.precog.PrecogDivisionRepository
import ch.ejpd.example.precrime.infrastructure.integration.persistence.OptimisticLockingException
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

@IntegrationTest
class OptimisticLockingTest(
    @Autowired private val precogRepository: PrecogDivisionRepository
) {

    @Test
    @Transactional
    fun `concurrent update should throw OptimisticLockingException`() {
        // GIVEN
        val division1 = precogRepository.findSingleton()
        val division2 = precogRepository.findSingleton()

        // WHEN
        division1.recordPrevention()
        precogRepository.save(division1)

        // THEN
        division2.recordPrevention()
        assertThrows(OptimisticLockingException::class.java) {
            precogRepository.save(division2)
        }
    }
}
