package ch.ejpd.example.precrime.infrastructure.facade.rest

import ch.ejpd.example.precrime.infrastructure.facade.rest.api.UserApi
import ch.ejpd.example.precrime.infrastructure.facade.rest.model.UserResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController : UserApi {

    override fun getCurrentUser(): ResponseEntity<UserResponse> {
        val authentication = SecurityContextHolder.getContext().authentication
        return ResponseEntity.ok(UserResponse(username = authentication?.name ?: "anonymous"))
    }
}
