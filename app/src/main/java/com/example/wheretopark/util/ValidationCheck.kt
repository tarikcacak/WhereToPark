package com.example.wheretopark.util

import android.util.Patterns

fun validateEmail(email: String): RegisterValidation {
    if (email.isEmpty())
        return RegisterValidation.Failed("Email can't be empty")

    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        return RegisterValidation.Failed("Wrong email format")

    return RegisterValidation.Success
}

fun validatePassword(password: String): RegisterValidation {
    if(password.isEmpty())
        return RegisterValidation.Failed("Password can't be empty")

    if(password.length < 6)
        return RegisterValidation.Failed("Password should contain 8 char.")

    return RegisterValidation.Success
}

fun validateUser(username: String): RegisterValidation {
    if (username.isEmpty())
        return RegisterValidation.Failed("Username can't be empty")

    if (username.length < 3)
        return RegisterValidation.Failed("Username must have 3 characters")

    return RegisterValidation.Success
}

fun validatePlates(plates: String): RegisterValidation {
    val regex = Regex("""^[A-Z]\d{2}-[A-Z]-[A-Z\d]{1,3}$""")

    if (plates.isEmpty()) {
        return RegisterValidation.Failed("Plates can't be empty!")
    }

    if (!regex.matches(plates)) {
        return RegisterValidation.Failed("Wrong format!")
    }

    return RegisterValidation.Success
}