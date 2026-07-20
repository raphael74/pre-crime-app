package org.example.precrime.infrastructure.facade.rest

import org.example.precrime.infrastructure.facade.rest.api.UserApi
import org.example.precrime.infrastructure.facade.rest.model.UserResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class UserController : UserApi {

    override fun getCurrentUser(): ResponseEntity<UserResponse> {
        val authentication = SecurityContextHolder.getContext().authentication
        return ResponseEntity.ok(UserResponse(username = authentication?.name ?: "anonymous"))
    }
}
