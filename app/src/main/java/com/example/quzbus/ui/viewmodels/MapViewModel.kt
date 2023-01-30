package com.example.quzbus.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quzbus.domain.repository.AuthRepository
import com.example.quzbus.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _getSmsCodeResponse: MutableLiveData<NetworkResult<String>> = MutableLiveData()
    val getSmsResponse: LiveData<NetworkResult<String>> = _getSmsCodeResponse

    fun getSmsCode(phoneNumber: String) {
        viewModelScope.launch {
            _getSmsCodeResponse.postValue(NetworkResult.Loading())
            _getSmsCodeResponse.postValue(authRepository.getSmsCode(phoneNumber))
        }
    }
}