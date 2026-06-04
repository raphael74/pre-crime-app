package ch.ejpd.example.precrime.infrastructure.facade.rest

import ch.ejpd.example.precrime.application.CreateVisionCommand
import ch.ejpd.example.precrime.application.PreCrimeApplicationService
import ch.ejpd.example.precrime.domain.prearrest.PreArrestId
import ch.ejpd.example.precrime.domain.vision.CrimeType
import ch.ejpd.example.precrime.infrastructure.facade.rest.api.PreCrimeApi
import ch.ejpd.example.precrime.infrastructure.facade.rest.model.*
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
    override fun arrestExecuted(arrestExecutedRequest: ArrestExecutedRequest): ResponseEntity<Unit> {
        applicationService.executePreArrest(PreArrestId(arrestExecutedRequest.preArrestId))
        return ResponseEntity.ok().build()
    }

    @PreAuthorize("hasRole('$USER_ROLE')")
    override fun arrestCancelled(arrestCancelRequest: ArrestCancelRequest): ResponseEntity<Unit> {
        applicationService.cancelPreArrest(PreArrestId(arrestCancelRequest.preArrestId))
        return ResponseEntity.ok().build()
    }

    @PreAuthorize("hasRole('$USER_ROLE')")
    override fun getArrestsExecuted(): ResponseEntity<List<PreArrestResponse>> {
        val arrests = applicationService.getAllExecutedPreArrests().map { (arrest, perpetrator) ->
            PreArrestResponse(
                id = arrest.id.value,
                visionId = arrest.visionId.value,
                perpetratorId = arrest.perpetratorId.value,
                firstName = perpetrator.firstName,
                lastName = perpetrator.lastName,
                preArrestIssuedDate = arrest.preArrestIssuedDate,
                preArrestDate = arrest.preArrestDate,
                status = PreArrestResponse.Status.valueOf(arrest.status.name)
            )
        }
        return ResponseEntity.ok(arrests)
    }

    @PreAuthorize("hasRole('$USER_ROLE')")
    override fun getArrestsPending(): ResponseEntity<List<PreArrestResponse>> {
        val arrests = applicationService.getAllPendingPreArrests().map { (arrest, perpetrator) ->
            PreArrestResponse(
                id = arrest.id.value,
                visionId = arrest.visionId.value,
                perpetratorId = arrest.perpetratorId.value,
                firstName = perpetrator.firstName,
                lastName = perpetrator.lastName,
                preArrestIssuedDate = arrest.preArrestIssuedDate,
                preArrestDate = arrest.preArrestDate,
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
        val cmd = CreateVisionCommand(
            createVisionRequest.perpetratorFirstName,
            createVisionRequest.perpetratorLastName,
            CrimeType.valueOf(createVisionRequest.crimeType.value)
        )
        val visionId = applicationService.triggerVision(cmd)
        return ResponseEntity.status(HttpStatus.CREATED).body(
            CreateVisionResponse(
                visionId = visionId.value,
            )
        )
    }
}
