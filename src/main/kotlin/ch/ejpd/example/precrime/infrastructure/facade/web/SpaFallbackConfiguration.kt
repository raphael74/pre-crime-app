package ch.ejpd.example.precrime.infrastructure.facade.web

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class SpaController {
    @GetMapping("/login")
    fun spaFallback(): String = "forward:/index.html"
}
