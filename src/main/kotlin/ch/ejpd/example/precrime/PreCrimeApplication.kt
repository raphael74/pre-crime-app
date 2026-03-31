package ch.ejpd.example.precrime

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class PreCrimeApplication

fun main(args: Array<String>) {
    runApplication<PreCrimeApplication>(*args)
}
