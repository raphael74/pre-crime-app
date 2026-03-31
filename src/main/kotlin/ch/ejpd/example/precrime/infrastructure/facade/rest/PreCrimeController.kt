package ch.ejpd.example.precrime.infrastructure.facade.rest

import ch.ejpd.example.precrime.application.PreCrimeApplicationService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/pre-crime")
class PreCrimeController(private val applicationService: PreCrimeApplicationService) {

    @PostMapping("/vision")
    fun createVision(@RequestParam perpetrator: String, @RequestParam crimeType: String): String {
        applicationService.triggerVision(perpetrator, crimeType)
        return "Vision triggered for $perpetrator. The Pre-Crime unit is on its way!"
    }
}
