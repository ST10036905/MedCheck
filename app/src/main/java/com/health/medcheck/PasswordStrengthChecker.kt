package com.health.medcheck

object PasswordStrengthChecker {

    enum class Strength {
        WEAK, MEDIUM, STRONG, VERY_STRONG
    }

    fun calculateStrength(password: String): Strength {
        var score = 0

        // Check length
        if (password.length >= 8) score++
        if (password.length >= 12) score++

        // Check for uppercase letters
        if (password.matches(Regex(".*[A-Z].*"))) score++

        // Check for lowercase letters
        if (password.matches(Regex(".*[a-z].*"))) score++

        // Check for numbers
        if (password.matches(Regex(".*\\d.*"))) score++

        // Check for special characters
        if (password.matches(Regex(".*[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*"))) score++

        return when {
            score >= 6 -> Strength.VERY_STRONG
            score >= 4 -> Strength.STRONG
            score >= 2 -> Strength.MEDIUM
            else -> Strength.WEAK
        }
    }

    fun meetsRequirements(password: String): Boolean {
        return password.length >= 8 &&
                password.matches(Regex(".*[A-Z].*")) &&
                password.matches(Regex(".*[a-z].*")) &&
                password.matches(Regex(".*\\d.*")) &&
                password.matches(Regex(".*[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*"))
    }
}