package com.revolage.quzbus.ui.viewmodels

data class AuthFormState(
    val phoneNumberError: Int? = null,
    val smsCodeError: Int? = null,
    val isDataValid: Boolean = false
)
