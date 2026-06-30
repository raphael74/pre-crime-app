package ch.ejpd.example.precrime.infrastructure.facade.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler

@Configuration
@EnableWebSecurity
class SecurityConfiguration {

    companion object {
        const val USER_ROLE = "USER"
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            csrf {
                disable()
            }
            authorizeHttpRequests {
                authorize("/api/**", authenticated)
                authorize(anyRequest, permitAll)
            }
            formLogin {
                disable()
            }
            httpBasic { }
            logout {
                logoutUrl = "/api/logout"
                logoutSuccessHandler = HttpStatusReturningLogoutSuccessHandler()
            }
        }

        return http.build()
    }

    @Bean
    fun userDetailsService(): UserDetailsService {
        @Suppress("DEPRECATION")
        val userDetails = User.withDefaultPasswordEncoder()
            .username("precog")
            .password("agatha")
            .roles(USER_ROLE)
            .build()
        return InMemoryUserDetailsManager(userDetails)
    }
}