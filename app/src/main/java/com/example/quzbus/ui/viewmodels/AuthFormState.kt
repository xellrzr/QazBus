package com.example.quzbus.domain.models

data class AuthFormState(
    val phoneNumberError: Int? = null,
    val smsCodeError: Int? = null,
    val isDataValid: Boolean = false
)
