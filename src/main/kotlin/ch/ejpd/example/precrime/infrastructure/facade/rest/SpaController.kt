package ch.ejpd.example.precrime.infrastructure.facade.rest

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class SpaController {
    @GetMapping("/login")
    fun spaFallback(): String = "forward:/index.html"
}