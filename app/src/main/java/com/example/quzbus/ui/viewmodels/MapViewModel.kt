package com.example.quzbus.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quzbus.R
import com.example.quzbus.domain.models.AuthFormState
import com.example.quzbus.data.models.response.Message
import com.example.quzbus.domain.models.RouteState
import com.example.quzbus.domain.models.routes.Route
import com.example.quzbus.domain.repository.AuthRepository
import com.example.quzbus.domain.repository.CitiesRepository
import com.example.quzbus.domain.repository.RoutesRepository
import com.example.quzbus.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val citiesRepository: CitiesRepository,
    private val routesRepository: RoutesRepository,
) : ViewModel() {

    //Хранит ответ на запрос получение списка городов
    private val _getCitiesResponse: MutableLiveData<NetworkResult<Message>> = MutableLiveData()
    val getCitiesResponse: LiveData<NetworkResult<Message>> = _getCitiesResponse

    //Хранит ответ на запрос получение SMS - кода
    private val _getSmsCodeResponse: MutableLiveData<NetworkResult<Message>> = MutableLiveData()

    //Хранит ответ на запрос получение авторизации
    private val _getAuthResponse: MutableLiveData<NetworkResult<Message>> = MutableLiveData()
    val getAuthResponse: LiveData<NetworkResult<Message>> = _getAuthResponse

    //Хранит стейт и проверку на заполненность данных номер телефона и SMS код
    private val _authFormState: MutableLiveData<AuthFormState> = MutableLiveData()

    //Хранит стейт со всеми маршрутами доступными для города
    //ХэшМап ключ - номер маршрута, значение - маршрут для этого номера
    private val _routeState: MutableLiveData<RouteState> = MutableLiveData()
    val routeState: LiveData<RouteState> = _routeState

    //Метод для получения списка городов
    fun getCities() {
        viewModelScope.launch {
            _getCitiesResponse.postValue(NetworkResult.Loading())
            _getCitiesResponse.postValue(citiesRepository.getCities())
        }
    }

    //Метод для получения SMS - кода
    fun getSmsCode(phoneNumber: String) {
        viewModelScope.launch {
            _getSmsCodeResponse.postValue(NetworkResult.Loading())
            _getSmsCodeResponse.postValue(authRepository.getSmsCode(phoneNumber))
        }
    }

    //Метод для авторизации
    fun getAuth(phoneNumber: String, password: String) {
        viewModelScope.launch {
            _getAuthResponse.postValue(NetworkResult.Loading())
            _getAuthResponse.postValue(authRepository.getAuth(phoneNumber, password))
        }
    }

    fun getRoutes(cityId: Int) {
        viewModelScope.launch {
            when(val result = routesRepository.getRoutes(cityId)) {
                is NetworkResult.Success -> {
                    val routes = hashMapOf<String, Route>()
                    val dataRoutes = result.data?.routes ?: emptyList()
                    for (route in dataRoutes) {
                        routes[route.name] = route
                    }
                    _routeState.postValue(
                        RouteState(
                            routes = routes,
                            isLoading = false
                        )
                    )
                }
                is NetworkResult.Error -> {
                    val routes = hashMapOf<String, Route>()
                    _routeState.postValue(
                        RouteState(
                            routes = routes,
                            error = "Can't load routes",
                            isLoading = false
                        )
                    )
                }
                is NetworkResult.Loading -> {
                    _routeState.postValue(
                        RouteState(
                            isLoading = true
                        )
                    )
                }
            }
        }
    }

    fun getRoute(route: String) {
        viewModelScope.launch {
            val result = routesRepository.getRoute(route)
            val routes = _routeState.value?.routes
            when(result) {
                is NetworkResult.Success -> {
                    if (routes?.containsKey(route) == true) {
                        routes[route]?.let { routeObj ->
                            routeObj.routeA = result.data?.La?.map { it.toRouteCoordinates() }.orEmpty()
                            routeObj.routeB = result.data?.Lb?.map { it.toRouteCoordinates() }.orEmpty()
                            routeObj.routeStopsA = result.data?.Sa?.map { it.toRouteStops() }.orEmpty()
                            routeObj.routeStopsB = result.data?.Sb?.map { it.toRouteStops() }.orEmpty()
                            routeObj.routeStart = result.data?.Na
                            routeObj.routeFinish = result.data?.Nb
                        }
                    }
                    _routeState.postValue(
                        routes?.let {
                            RouteState(
                                routes = it,
                                isLoading = false
                            )
                        }
                    )
                }
                is NetworkResult.Error -> {
                    val newRoutes = hashMapOf<String, Route>()
                    _routeState.postValue(
                        RouteState(
                            routes = newRoutes,
                            error = "Can't load routes",
                            isLoading = false
                        )
                    )
                }
                is NetworkResult.Loading -> {
                    _routeState.postValue(
                        RouteState(
                            isLoading = true
                        )
                    )
                }
            }
        }
    }

    //Метод для проверки авторизован ли пользователь
    fun isUserLoggedIn(): Boolean {
        return authRepository.isUserLoggedIn()
    }

    //Метод для проверки изменения данных в полях авторизации
    fun authDataChanged(phoneNumber: String, smsCode: String) {
        if (!isPhoneNumberValid(phoneNumber)) {
            _authFormState.value = AuthFormState(phoneNumberError = R.string.incorrect_phone_number)
        } else if (!isSmsCodeValid(smsCode)) {
            _authFormState.value = AuthFormState(smsCodeError = R.string.incorrect_sms_code)
        } else {
            _authFormState.value = AuthFormState(isDataValid = true)
        }
    }

    //Метод для проверки заполненности поля ввода номера телефона
    private fun isPhoneNumberValid(phoneNumber: String): Boolean {
           return phoneNumber.isNotBlank()
    }

    //Метод для проверки длины введенного SMS - кода
    private fun isSmsCodeValid(smsCode: String): Boolean {
        return smsCode.length == 6
    }

}