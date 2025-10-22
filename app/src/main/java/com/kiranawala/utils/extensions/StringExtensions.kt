package com.kiranawala.utils.extensions

fun String.isValidEmail(): Boolean {
    val emailPattern = Regex("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")
    return this.matches(emailPattern)
}

fun String.isValidPhone(): Boolean {
    return this.length == 10 && this.all { it.isDigit() }
}

fun String.capitalizeFirstLetter(): String {
    return if (this.isNotEmpty()) {
        this[0].uppercaseChar() + this.substring(1).lowercase()
    } else {
        this
    }
}