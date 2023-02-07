package com.example.quzbus.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quzbus.R
import com.example.quzbus.data.models.AuthFormState
import com.example.quzbus.data.models.response.Message
import com.example.quzbus.domain.models.RouteState
import com.example.quzbus.domain.models.buses.RouteBusesState
import com.example.quzbus.domain.models.busroute.BusRoute
import com.example.quzbus.domain.models.routes.Route
import com.example.quzbus.domain.models.routes.Routes
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

    //Хранит ответ на запрос получение списка городов
    private val _getCitiesResponse: MutableLiveData<NetworkResult<Message>> = MutableLiveData()
    val getCitiesResponse: LiveData<NetworkResult<Message>> = _getCitiesResponse

    //Хранит ответ на запрос получение SMS - кода
    private val _getSmsCodeResponse: MutableLiveData<NetworkResult<Message>> = MutableLiveData()
    val getSmsResponse: LiveData<NetworkResult<Message>> = _getSmsCodeResponse

    //Хранит ответ на запрос получение авторизации
    private val _getAuthResponse: MutableLiveData<NetworkResult<Message>> = MutableLiveData()
    val getAuthResponse: LiveData<NetworkResult<Message>> = _getAuthResponse

    //Хранит стейт и проверку на заполненность данных номер телефона и SMS код
    private val _authFormState: MutableLiveData<AuthFormState> = MutableLiveData()
    val authFormState: LiveData<AuthFormState> = _authFormState

    //Хранит ответ на запрос получение списка маршрутов(номера автобусов) для выбранного города
    private val _getRoutes: MutableLiveData<NetworkResult<Routes>> = MutableLiveData()
    val getRoutes: LiveData<NetworkResult<Routes>> = _getRoutes

    //Хранит ответ на запрос получение маршрута для выбранного автобуса
    private val _getSingleRoute: MutableLiveData<NetworkResult<BusRoute>> = MutableLiveData()
    val getSingleRoute: LiveData<NetworkResult<BusRoute>> = _getSingleRoute

    //Хранит ответ на запрос получение списка автобусов по выбранному маршруту
    private val _getSingleRouteBuses: MutableLiveData<RouteBusesState> = MutableLiveData()
    val getSingleRouteBuses: LiveData<RouteBusesState> = _getSingleRouteBuses

    //Хранит стейт со всеми маршрутами доступными для города
    //ХэшМап ключ - номер маршрута, значение - маршрут для этого номера
    private val _routeState: MutableLiveData<RouteState> = MutableLiveData()
    val routeState: LiveData<RouteState> = _routeState

    fun getSingleRouteBuses(route: String) {
        viewModelScope.launch {
            val result = singleRouteRepository.getBusesRoutes(route)
            when(result) {
                is NetworkResult.Success -> {
                    _getSingleRouteBuses.postValue(
                        RouteBusesState(
                            busRoutes = result.data?.busRoutes ?: emptyList(),
                            isLoading = false
                        )
                    )
                }
                is NetworkResult.Error -> {
                    _getSingleRouteBuses.postValue(
                        RouteBusesState(
                            busRoutes = result.data?.busRoutes ?: emptyList(),
                            isLoading = false
                        )
                    )
                }
                is NetworkResult.Loading -> {
                    _getSingleRouteBuses.postValue(
                        RouteBusesState(
                            busRoutes = result.data?.busRoutes ?: emptyList(),
                            isLoading = true
                        )
                    )
                }
            }
        }
    }

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

    //Метод для получения маршрутов выбранного города
    fun getRoutes(cityId: Int) {
        viewModelScope.launch {
            _getRoutes.postValue(NetworkResult.Loading())
            _getRoutes.postValue(routesRepository.getRoutes(cityId))
        }
    }

    fun getRoutesX(cityId: Int) {
        viewModelScope.launch {
            val result = routesRepository.getRoutes(cityId)

            when(result) {

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

    fun getSingleRouteX(route: String) {
        viewModelScope.launch {
            val result = singleRouteRepository.getSingleRoute(route)
            val routes = _routeState.value?.routes
//            val newRoutes = hashMapOf<String, Route>()
            when(result) {

                is NetworkResult.Success -> {
                    if (routes?.containsKey(route) == true) {
//                        newRoutes[route]?.routeA = result.data?.routeA ?: emptyList()

                        routes[route]?.routeA = result.data?.routeA ?: emptyList()
                        routes[route]?.routeB = result.data?.routeB ?: emptyList()
                        routes[route]?.routeStopsA = result.data?.routeStopsA ?: emptyList()
                        routes[route]?.routeStopsB = result.data?.routeStopsB ?: emptyList()
                        routes[route]?.routeStart = result.data?.routeStart
                        routes[route]?.routeFinish = result.data?.routeFinish
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

    //Метод для получения маршрута выбранного номера автобуса
    fun getSingleRoute(route: String) {
        viewModelScope.launch {
            _getSingleRoute.postValue(NetworkResult.Loading())
            _getSingleRoute.postValue(singleRouteRepository.getSingleRoute(route))
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