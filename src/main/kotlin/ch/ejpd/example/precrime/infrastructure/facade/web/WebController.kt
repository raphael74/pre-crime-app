package ch.ejpd.example.precrime.infrastructure.facade.web

import ch.ejpd.example.precrime.domain.vision.CrimeType
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class WebController {

    @GetMapping("/")
    fun index(model: Model): String {
        model.addAttribute("crimeTypes", CrimeType.entries.map { it.name })
        return "hud"
    }

    @GetMapping("/login")
    fun login(): String {
        return "login"
    }
}
