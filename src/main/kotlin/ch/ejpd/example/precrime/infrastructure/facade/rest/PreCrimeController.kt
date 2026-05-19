package ch.ejpd.example.precrime.infrastructure.facade.rest

import ch.ejpd.example.precrime.application.PreCrimeApplicationService
import ch.ejpd.example.precrime.infrastructure.facade.rest.api.PreCrimeApi
import ch.ejpd.example.precrime.infrastructure.facade.rest.model.CreateVisionRequest
import ch.ejpd.example.precrime.infrastructure.facade.rest.model.CreateVisionResponse
import ch.ejpd.example.precrime.infrastructure.facade.security.SecurityConfiguration.Companion.USER_ROLE
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class PreCrimeController(private val applicationService: PreCrimeApplicationService) : PreCrimeApi {

    @PreAuthorize("hasRole('$USER_ROLE')")
    override fun getStats(): ResponseEntity<Int> {
        return ResponseEntity.ok(applicationService.getPreventedCrimesCount())
    }

    @PreAuthorize("hasRole('$USER_ROLE')")
    override fun createVision(createVisionRequest: CreateVisionRequest): ResponseEntity<CreateVisionResponse> {
        val visionId =
            applicationService.triggerVision(createVisionRequest.perpetrator, createVisionRequest.crimeType)
        return ResponseEntity.status(HttpStatus.CREATED).body(
            CreateVisionResponse(
                visionId = visionId.value,
                message = "Vision triggered for ${createVisionRequest.perpetrator}. The Pre-Crime unit is on its way!"
            )
        )
    }
}
