package com.finance.savvycents.utilities

import android.util.Patterns

object ValidationUtils {
    fun validateName(name: String): Validator {
        return if (name.isBlank()) {
            Validator.Error("Name cannot be empty")
        } else {
            Validator.Success()
        }
    }

    fun validateEmail(email: String): Validator {
        return if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Validator.Error("Invalid email format")
        } else {
            Validator.Success()
        }
    }

    fun validatePassword(password: String): Validator {
        return if (password.length < 6) {
            Validator.Error("Password must be at least 6 characters")
        } else {
            Validator.Success()
        }
    }

    fun validateConfirmPassword(password: String, confirmPassword: String): Validator {
        return if (password != confirmPassword) {
            Validator.Error("Passwords do not match")
        } else {
            Validator.Success()
        }
    }

    fun validatePhone(phone: String): Validator {
        return if (!Patterns.PHONE.matcher(phone).matches()) {
            Validator.Error("Invalid phone number")
        } else {
            Validator.Success()
        }
    }
}
