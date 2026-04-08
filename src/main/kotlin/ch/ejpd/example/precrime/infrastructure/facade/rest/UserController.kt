package ch.ejpd.example.precrime.infrastructure.facade.rest

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
@RequestMapping("/api/user")
class UserController {

    @GetMapping
    fun getCurrentUser(principal: Principal): Map<String, String> {
        return mapOf("username" to principal.name)
    }
}
