package com.example.quzbus.data.models

data class AuthFormState(
    val phoneNumberError: Int? = null,
    val smsCodeError: Int? = null,
    val isDataValid: Boolean = false
)
