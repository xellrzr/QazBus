package com.example.quzbus.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quzbus.R
import com.example.quzbus.data.models.AuthFormState
import com.example.quzbus.data.models.response.Message
import com.example.quzbus.data.models.response.Routes
import com.example.quzbus.data.models.response.singleroute.BusRoute
import com.example.quzbus.domain.repository.AuthRepository
import com.example.quzbus.domain.repository.CitiesRepository
import com.example.quzbus.domain.repository.RoutesRepository
import com.example.quzbus.domain.repository.SingleRouteRepository
import com.example.quzbus.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val citiesRepository: CitiesRepository,
    private val routesRepository: RoutesRepository,
    private val singleRouteRepository: SingleRouteRepository
) : ViewModel() {

    private val _getCitiesResponse: MutableLiveData<NetworkResult<Message>> = MutableLiveData()
    val getCitiesResponse: LiveData<NetworkResult<Message>> = _getCitiesResponse

    private val _getSmsCodeResponse: MutableLiveData<NetworkResult<Message>> = MutableLiveData()
    val getSmsResponse: LiveData<NetworkResult<Message>> = _getSmsCodeResponse

    private val _getAuthResponse: MutableLiveData<NetworkResult<Message>> = MutableLiveData()
    val getAuthResponse: LiveData<NetworkResult<Message>> = _getAuthResponse

    private val _authFormState: MutableLiveData<AuthFormState> = MutableLiveData()
    val authFormState: LiveData<AuthFormState> = _authFormState

    private val _getRoutesResponse: MutableLiveData<NetworkResult<Routes>> = MutableLiveData()
    val getRoutesResponse: LiveData<NetworkResult<Routes>> = _getRoutesResponse

    private val _getSingleRouteResponse: MutableLiveData<NetworkResult<BusRoute>> = MutableLiveData()
    val getSingleRouteResponse: LiveData<NetworkResult<BusRoute>> = _getSingleRouteResponse

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

    fun getAuth(phoneNumber: String, password: String) {
        viewModelScope.launch {
            _getAuthResponse.postValue(NetworkResult.Loading())
            _getAuthResponse.postValue(authRepository.getAuth(phoneNumber, password))
        }
    }

    fun getRoutes(cityId: Int) {
        viewModelScope.launch {
            _getRoutesResponse.postValue(NetworkResult.Loading())
            _getRoutesResponse.postValue(routesRepository.getRoutes(cityId))
        }
    }

    fun getSingleRoute(route: String) {
        viewModelScope.launch {
            _getSingleRouteResponse.postValue(NetworkResult.Loading())
            _getSingleRouteResponse.postValue(singleRouteRepository.getSingleRoute(route))
        }
    }

    fun setSelectCity(city: String) {
        citiesRepository.setSelectCity(city)
    }

    fun setCityId(cityId: Int) {
        citiesRepository.setCityId(cityId)
    }

    fun isUserLoggedIn(): Boolean {
        return authRepository.isUserLoggedIn()
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