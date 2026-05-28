package ch.ejpd.example.precrime.domain.apology

import ch.ejpd.example.precrime.domain.vision.Vision

interface PreApologyLetterService {
    fun generateLetterText(vision: Vision, compensation: Compensation): String
}
