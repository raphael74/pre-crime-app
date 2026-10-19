package org.example.precrime.infrastructure.facade.security

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
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter.HeaderValue

@Configuration
@EnableWebSecurity
class SecurityConfiguration {
    companion object {
        const val USER_ROLE = "USER"
        private const val HSTS_MAX_AGE_SECONDS = 31536000L // 1 year in seconds
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            csrf {
                disable()
            }
            headers {
                frameOptions {
                    deny = true
                }
                contentTypeOptions { }
                xssProtection {
                    headerValue = HeaderValue.DISABLED
                }
                referrerPolicy {
                    policy =
                        org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy
                            .STRICT_ORIGIN_WHEN_CROSS_ORIGIN
                }
                httpStrictTransportSecurity {
                    includeSubDomains = true
                    maxAgeInSeconds = HSTS_MAX_AGE_SECONDS
                    preload = true
                }
                contentSecurityPolicy {
                    policyDirectives =
                        "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; " +
                        "img-src 'self' data:; font-src 'self'; connect-src 'self'; frame-ancestors 'none'; form-action 'self'"
                }
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
        val userDetails =
            User
                .withDefaultPasswordEncoder()
                .username("precog")
                .password("agatha")
                .roles(USER_ROLE)
                .build()
        return InMemoryUserDetailsManager(userDetails)
    }
}
