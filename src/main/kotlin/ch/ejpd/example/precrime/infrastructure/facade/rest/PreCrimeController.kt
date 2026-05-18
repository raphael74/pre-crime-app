package ch.ejpd.example.precrime.infrastructure.facade.rest

import ch.ejpd.example.precrime.application.PreCrimeApplicationService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/pre-crime")
class PreCrimeController(private val applicationService: PreCrimeApplicationService) {

    @GetMapping("/stats")
    fun getStats(): Int {
        return applicationService.getPreventedCrimesCount()
    }

    @PostMapping("/vision")
    fun createVision(@RequestBody request: CreateVisionRequest): ResponseEntity<CreateVisionResponse> {
        val visionId = applicationService.triggerVision(request.perpetrator, request.crimeType)
        return ResponseEntity.status(HttpStatus.CREATED).body(
            CreateVisionResponse(
                visionId = visionId.value,
                message = "Vision triggered for ${request.perpetrator}. The Pre-Crime unit is on its way!"
            )
        )
    }
}

data class CreateVisionRequest(
    val perpetrator: String,
    val crimeType: String
)

data class CreateVisionResponse(
    val visionId: UUID,
    val message: String
)
