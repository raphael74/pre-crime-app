package ch.ejpd.example.precrime.infrastructure.facade.rest

import ch.ejpd.example.precrime.application.PreCrimeApplicationService
import ch.ejpd.example.precrime.domain.vision.CrimeType
import ch.ejpd.example.precrime.infrastructure.facade.rest.api.PreCrimeApi
import ch.ejpd.example.precrime.infrastructure.facade.rest.model.CreateVisionRequest
import ch.ejpd.example.precrime.infrastructure.facade.rest.model.CreateVisionResponse
import ch.ejpd.example.precrime.infrastructure.facade.rest.model.PreApologyResponse
import ch.ejpd.example.precrime.infrastructure.facade.rest.model.PreArrestResponse
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
    override fun getArrests(): ResponseEntity<List<PreArrestResponse>> {
        val arrests = applicationService.getAllPreArrests().map { (arrest, perpetrator) ->
            PreArrestResponse(
                id = arrest.id.value,
                visionId = arrest.visionId.value,
                firstName = perpetrator.firstName,
                lastName = perpetrator.lastName,
                status = PreArrestResponse.Status.valueOf(arrest.status.name)
            )
        }
        return ResponseEntity.ok(arrests)
    }

    @PreAuthorize("hasRole('$USER_ROLE')")
    override fun getStats(): ResponseEntity<Int> {
        return ResponseEntity.ok(applicationService.getPreventedCrimesCount())
    }

    @PreAuthorize("hasRole('$USER_ROLE')")
    override fun getApologies(): ResponseEntity<List<PreApologyResponse>> {
        val apologies = applicationService.getAllApologies().map { (apology, perpetrator) ->
            PreApologyResponse(
                id = apology.id.value,
                visionId = apology.visionId.value,
                lastName = perpetrator.lastName,
                firstName = perpetrator.firstName,
                baseAmount = java.math.BigDecimal.valueOf(apology.compensation.baseAmount),
                jetpackFuelDeduction = java.math.BigDecimal.valueOf(apology.compensation.jetpackFuelDeduction),
                haloRentalFee = java.math.BigDecimal.valueOf(apology.compensation.haloRentalFee),
                netPayout = java.math.BigDecimal.valueOf(apology.compensation.netPayout),
                apologyText = apology.apologyLetter.text
            )
        }
        return ResponseEntity.ok(apologies)
    }

    @PreAuthorize("hasRole('$USER_ROLE')")
    override fun createVision(createVisionRequest: CreateVisionRequest): ResponseEntity<CreateVisionResponse> {
        val visionId =
            applicationService.triggerVision(
                createVisionRequest.perpetratorFirstName,
                createVisionRequest.perpetratorLastName,
                CrimeType.valueOf(createVisionRequest.crimeType.value)
            )
        return ResponseEntity.status(HttpStatus.CREATED).body(
            CreateVisionResponse(
                visionId = visionId.value,
                message = "Vision triggered for ${createVisionRequest.perpetratorFirstName} ${createVisionRequest.perpetratorLastName}. The Pre-Crime unit is on its way!"
            )
        )
    }
}
