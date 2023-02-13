package com.example.quzbus.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quzbus.R
import com.example.quzbus.data.models.response.Message
import com.example.quzbus.domain.models.routes.Direction
import com.example.quzbus.domain.models.routes.Pallet
import com.example.quzbus.domain.models.routes.Route
import com.example.quzbus.domain.repository.AuthRepository
import com.example.quzbus.domain.repository.CitiesRepository
import com.example.quzbus.domain.repository.RoutesRepository
import com.example.quzbus.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.schedule

@HiltViewModel
class MapViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val citiesRepository: CitiesRepository,
    private val routesRepository: RoutesRepository,
) : ViewModel() {

    private var job: Job? = null
    private var pallet = Pallet.MAGENTA
    private val pool = hashMapOf<Pallet, Route>()

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

    //Сброс сохранненого города
    fun changeCity() {
        val routes = hashMapOf<String, Route>()
        citiesRepository.setCityId(0)
        _routeState.postValue(
            RouteState(
                routes = routes
            )
        )
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

    //По нажатию проверяем есть ли переданный маршрут в коллекции
    fun selectRoute(route: String): Boolean {
        val model = _routeState.value?.routes?.get(route)
        //есть есть и направление А - тогда меняем направление на Б
        if (model != null) {
            if (model.selectedDirection != null) {
                if(model.selectedDirection == Direction.DIRECTION_A) {
                    model.selectedDirection = Direction.DIRECTION_B
                    //Если маршрута нет - обнуяем его направление и очищаем паллетку
                } else {
                    model.selectedDirection = null
                    model.pallet?.let { drain(it) }
                }
                //Если направление равно нулю - тогда задаем направление движение на А
            } else {
                val success = insert(model)
                if (success) {
                    model.selectedDirection = Direction.DIRECTION_A
                } else {
                    return false
                }
            }
        }
        return true
    }

    //Получение маршрута для выбранного автобуса
    fun getRoute(route: String) {
        viewModelScope.launch {
            val result = routesRepository.getRoute(route)
            val routes = _routeState.value?.routes
            when(result) {
                is NetworkResult.Success -> {
                    if (routes?.containsKey(route) == true) {
                        routes[route]?.let { routeObj ->
                            result.data?.let { routeObj.fillFrom(it) }
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

    //Получение списка автобусов для выбранного маршрута
    private fun getBuses(route: String) {
        viewModelScope.launch {
            val result = routesRepository.getBuses(route)
            val routes = _routeState.value?.routes
            when(result) {
                is NetworkResult.Success -> {
                    if (routes?.containsKey(route) == true) {
                        routes[route]?.let { routeObj ->
                            result.data?.let { routeObj.fillBusesFrom(it) }
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
                            error = "Can't load buses",
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


    //Запуск таймера
    private fun startTimer() {
        job = viewModelScope.launch {
            while(true) {
                ping()
                delay(5_000)
            }
        }
    }

    //Отмена таймера
    private fun cancelTimer() {
        job?.cancel()
    }

    //Вставляем маршрут в паллетку
    private fun insert(route: Route): Boolean {
        if (pool.keys.size == Pallet.values().size) return false
        for (pallet in Pallet.values()){
            if (!pool.containsKey(pallet)) {
                route.pallet = pallet
                pool[pallet] = route
                break
            }
        }
        if (pool.keys.size == 1) {
            startTimer()
        }
        return true
    }

    //Получаем маршрут по цвету паллетки
    private fun drain(pallet: Pallet) {
        val route = pool[pallet]
        //Сбрасываем маршрут на 0
        route?.reset()
        //Удаляем из паллетки цвет
        pool.remove(pallet)
        //Если паллетка пустая - отменяем таймер
        if (pool.isEmpty()) {
            cancelTimer()
        }
        //Задать новое значение для маршрутов
        _routeState.postValue(
            routeState.value?.routes?.let {
                RouteState(
                    routes = it
                )
            }
        )
    }

    //Пинг маршрутов
    private fun ping() {
        var pingPallet = next(pallet)
        while (true) {
            val route = pool[pingPallet]
            if (route != null) {
                getRoute(route.name)
                Timer().schedule(1_000) {
                    getBuses(route.name)
                }
                pallet = pingPallet
                break
            } else {
                pingPallet = next(pingPallet)
            }
        }
    }

    //Присваивание цвета паллетки
    private fun next(palette: Pallet): Pallet {
        return when (palette) {
            Pallet.RED -> Pallet.GREEN
            Pallet.GREEN -> Pallet.BLUE
            Pallet.BLUE -> Pallet.YELLOW
            Pallet.YELLOW -> Pallet.PURPLE
            Pallet.PURPLE -> Pallet.MAGENTA
            Pallet.MAGENTA -> Pallet.RED
        }
    }

}