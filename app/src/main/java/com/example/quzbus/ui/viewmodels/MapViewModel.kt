package com.example.quzbus.ui.viewmodels

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quzbus.R
import com.example.quzbus.data.models.AuthFormState
import com.example.quzbus.data.models.response.Message
import com.example.quzbus.domain.repository.AuthRepository
import com.example.quzbus.domain.repository.CitiesRepository
import com.example.quzbus.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val citiesRepository: CitiesRepository
) : ViewModel() {

    private val _getCitiesResponse: MutableLiveData<NetworkResult<Message>> = MutableLiveData()
    val getCitiesResponse: LiveData<NetworkResult<Message>> = _getCitiesResponse

    private val _getSmsCodeResponse: MutableLiveData<NetworkResult<Message>> = MutableLiveData()
    val getSmsResponse: LiveData<NetworkResult<Message>> = _getSmsCodeResponse

    private val _getAuthResponse: MutableLiveData<NetworkResult<Message>> = MutableLiveData()
    val getAuthResponse: LiveData<NetworkResult<Message>> = _getAuthResponse

    private val _authFormState: MutableLiveData<AuthFormState> = MutableLiveData()
    val authFormState: LiveData<AuthFormState> = _authFormState

    fun getCities(){
        viewModelScope.launch {
            _getCitiesResponse.postValue(NetworkResult.Loading())
            _getCitiesResponse.postValue(citiesRepository.getCities())
        }
    }

    fun getSmsCode(phoneNumber: String) {
        viewModelScope.launch {
            _getSmsCodeResponse.postValue(NetworkResult.Loading())
            _getSmsCodeResponse.postValue(authRepository.getSmsCode(phoneNumber))
        }
    }

    fun getAuth(phoneNumber: String, language: String, password: String) {
        viewModelScope.launch {
            _getAuthResponse.postValue(NetworkResult.Loading())
            _getAuthResponse.postValue(authRepository.getAuth(phoneNumber, language, password))
        }
    }

    fun authDataChanged(phoneNumber: String, smsCode: String) {
        if (!isPhoneNumberValid(phoneNumber)) {
            _authFormState.value = AuthFormState(phoneNumberError = R.string.incorrect_phone_number)
        } else if (!isSmsCodeValid(smsCode)) {
            _authFormState.value = AuthFormState(smsCodeError = R.string.incorrect_sms_code)
        } else {
            _authFormState.value = AuthFormState(isDataValid = true)
        }
    }

    private fun isPhoneNumberValid(phoneNumber: String): Boolean {
           return phoneNumber.isNotBlank()
    }

    private fun isSmsCodeValid(smsCode: String): Boolean {
        return smsCode.length == 6
    }
}