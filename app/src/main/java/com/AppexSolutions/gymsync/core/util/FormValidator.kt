package com.AppexSolutions.gymsync.core.util

object FormValidator {
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun validatePassword(password: String): String? {
        if (password.length < 6) return "Mínimo 6 caracteres"
        if (!password.contains(Regex("[A-Z]"))) return "Falta una mayúscula"
        if (!password.contains(Regex("[0-9]"))) return "Falta un número"
        return null
    }
}