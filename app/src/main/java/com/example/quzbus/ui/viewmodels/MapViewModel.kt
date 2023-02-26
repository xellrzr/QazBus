package com.example.quzbus.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quzbus.R
import com.example.quzbus.data.models.response.Message
import com.example.quzbus.data.models.response.Region
import com.example.quzbus.domain.models.routes.Direction
import com.example.quzbus.domain.models.routes.Pallet
import com.example.quzbus.domain.models.routes.Route
import com.example.quzbus.domain.repository.*
import com.example.quzbus.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap
import kotlin.concurrent.schedule

@HiltViewModel
class MapViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val citiesRepository: CitiesRepository,
    private val routesRepository: RoutesRepository,
    private val resetUserDataRepository: ResetUserDataRepository,
    private val favoriteRouteRepository: FavoriteRouteRepository
) : ViewModel() {

    private var job: Job? = null
    private var pallet = Pallet.MAGENTA
    private val pool = hashMapOf<Pallet, Route>()
    private var routes = HashMap<String, Route>()

    //Хранит ответ на запрос получение SMS - кода
    private val _getSmsCodeResponse: MutableLiveData<NetworkResult<Message>> = MutableLiveData()
    val getSmsCodeResponse: LiveData<NetworkResult<Message>> = _getSmsCodeResponse

    //Хранит ответ на запрос получение авторизации
    private val _getAuthResponse: MutableLiveData<NetworkResult<Message>> = MutableLiveData()

    //Хранит стейт и проверку на заполненность данных номер телефона и SMS код
    private val _authFormState: MutableLiveData<AuthFormState> = MutableLiveData()

    //Хранит стейт со всеми маршрутами доступными для города
    private val _routeState: MutableLiveData<RouteState> = MutableLiveData()
    val routeState: LiveData<RouteState> = _routeState

    private val _sheetState: MutableLiveData<SheetState> = MutableLiveData()
    val sheetState: LiveData<SheetState> = _sheetState

    fun getCity(): String? {
        return citiesRepository.getCity()
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

    fun setup() {
        val isCitySelected = citiesRepository.isCitySelected()
        val isUserLoggedIn = authRepository.isUserLoggedIn()
        refreshSheetState(
            isCitySelected = isCitySelected,
            isAuthorized = isUserLoggedIn
        )
        if (isCitySelected && isUserLoggedIn) getRoutes() else getCities()
    }

    fun selectCity(cityName: String, cityId: Int) {
        citiesRepository.setSelectCity(cityName)
        citiesRepository.setCityId(cityId)
        refreshSheetState()
        if (authRepository.isUserLoggedIn()) getRoutes()
    }

    //Получение маршрута для выбранного автобуса
    fun getRoute(route: String) {
        viewModelScope.launch {
            when(val result = routesRepository.getRoute(route)) {
                is NetworkResult.Success -> {
                    routes[route]?.let { routeObj ->
                        //проверка - есть ли данные о маршруте движения
                        val isEmpty = routeObj.routeA.isEmpty()
                        //заполняем маршрут
                        result.data?.let { routeObj.fillFrom(it) }
                        if (isEmpty) {
                            refreshRouteState(
                                route = routeObj,
                                pallet = routeObj.pallet,
                                event = Event.ROUTE
                            )
                        }
                    }
                }
                is NetworkResult.Error -> {
                    refreshSheetState(error = "Ошибка получения маршрута")
                }
                is NetworkResult.Loading -> {
                    TODO()
                }
            }
        }
    }

    //По нажатию проверяем есть ли переданный маршрут в коллекции
    fun selectRoute(route: String): Boolean {
        val model = routes[route]

        if (model != null) {
            if (model.selectedDirection != null) {
                if(model.selectedDirection == Direction.DIRECTION_A) {
                    model.selectedDirection = Direction.DIRECTION_B
                    refreshRouteState(
                        route = model,
                        pallet = model.pallet,
                        event = Event.REDRAW
                    )
                    //Если маршрута нет - обнуяем его направление и очищаем паллетку
                } else {
                    model.selectedDirection = null
                    model.isSelected = false
                    model.pallet?.let { drain(it) }
                }
                //Если направление равно нулю - тогда задаем направление движение на А
            } else {
                val success = insert(model)
                if (success) {
                    model.selectedDirection = Direction.DIRECTION_A
                    model.isSelected = true
                } else {
                    return false
                }
            }
        }
        updateRoutesList()
        return true
    }

    fun favoriteRoute(route: String) {
        val model = routes[route]
        val name = model?.name ?: ""
        val cityId = citiesRepository.getCityId()
        model?.isFavorite = if (model != null) !model.isFavorite else false
        val isFavorite = model?.isFavorite ?: false
        favoriteRouteRepository.setFavoriteRoute(name, cityId, isFavorite)
        updateRoutesList()
    }

    fun resetCity() {
        refreshSheetState(
            isCitySelected = false
        )
        resetPool()
        citiesRepository.setCityId(0)
        citiesRepository.setSelectCity(null)
        getCities()
    }

    fun resetPhone() {
        refreshSheetState(
            isAuthorized = false,
            isCitySelected = false,
            cities = _sheetState.value?.cities ?: emptyList(),
            routes = emptyList()
        )
        resetPool()
        citiesRepository.setCityId(0)
        citiesRepository.setSelectCity(null)
        resetUserDataRepository.setPhoneNumber(null)
        resetUserDataRepository.setAccessToken(null)
    }

    private fun getRoutes() {
        viewModelScope.launch {
            var error: String? = null
            when(val result = routesRepository.getRoutes()) {
                is NetworkResult.Success -> {
                    val dataRoutes = result.data?.routes ?: emptyList()
                    for (route in dataRoutes) {
                        //проверяем по каждому маршруту, находится ли он в избранном
                        route.isFavorite = favoriteRouteRepository.getFavoriteRoute(
                            route.name, citiesRepository.getCityId())
                        //заполняем мапу маршрутами
                        routes[route.name] = route
                    }
                }
                is NetworkResult.Error -> {
                    error = "Ошибка получения маршрутов"
                    routes = hashMapOf()
                }
                is NetworkResult.Loading -> {
                    TODO()
                }
            }
            if (error != null) {
                refreshSheetState(error = error)
            } else {
                updateRoutesList()
            }
        }
    }

    //Метод для получения списка городов
    private fun getCities() {
        viewModelScope.launch {
            var cities = emptyList<Region>()
            var error: String? = null
            when(val result = citiesRepository.getCities()) {
                is NetworkResult.Loading -> {
                    TODO()
                }
                is NetworkResult.Success -> {
                    val regions = result.data?.regions
                    if (regions != null) {
                        cities = regions
                    } else {
                        error = "Список городой пустой"
                    }
                }
                is NetworkResult.Error -> {
                    error = "Ошибка получения списка городов"
                }
            }
            refreshSheetState(cities = cities, error = error)
        }
    }

    private fun resetPool() {
        refreshRouteState(
            route = Route("0","0"),
            pallet = pallet,
            event = Event.CLEAR
        )
        pool.clear()
        if (pool.isEmpty()) cancelTimer()
    }

    private fun refreshSheetState(
        isAuthorized: Boolean = authRepository.isUserLoggedIn(),
        isCitySelected: Boolean = citiesRepository.isCitySelected(),
        cities: List<Region> = emptyList(),
        routes: List<Route> = emptyList(),
        error: String? = null
    ) {
        _sheetState.postValue(
            SheetState(
                isAuthorized = isAuthorized,
                isCitySelected = isCitySelected,
                cities = cities,
                routes = routes,
                error = error
            )
        )
    }

    private fun refreshRouteState(
        route: Route? = null,
        pallet: Pallet? = null,
        event: Event,
    ) {
        _routeState.postValue(
            RouteState(
                route = route,
                pallet = pallet,
                event = event
            )
        )
    }

    //Сортировка маршрутов
    private fun updateRoutesList() {
        val routes = routes.values.map { it }
        val sortedList = routes.sortedWith(
            compareBy({ !it.isSelected }, { !it.isFavorite } ,{ it.name.toIntOrNull() }))
        refreshSheetState(routes = sortedList)
    }

    //Получение списка автобусов для выбранного маршрута
    private fun getBuses(route: String) {
        viewModelScope.launch {
            when(val result = routesRepository.getBuses(route)) {
                is NetworkResult.Success -> {
                    routes[route]?.let { routeObj ->
                        result.data?.let { routeObj.fillBusesFrom(it) }
                        refreshRouteState(
                            route = routeObj,
                            pallet = routeObj.pallet,
                            event = Event.BUS
                        )
                    }
                }
                is NetworkResult.Error -> {
                    refreshSheetState(error = "Ошибка получения списка автобусов")
                }
                is NetworkResult.Loading -> {
                    TODO()
                }
            }
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
            while(isActive) {
                ping()
                delay(5_000)
                yield()
            }
        }
    }

    //Отмена таймера
    private fun cancelTimer() {
        job?.cancel()
    }

    //Вставляем маршрут в паллетку
    private fun insert(route: Route): Boolean {
        //Если размер равен 6, прервать
        if (pool.keys.size == Pallet.values().size) return false
        for (pallet in Pallet.values()){
            //если пул не содержит цвет
            if (!pool.containsKey(pallet)) {
                //присвоить цвет для маршрута
                route.pallet = pallet
                //в пуле цветов - присвоить маршрут
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
        refreshRouteState(
            route = route,
            pallet = pallet,
            event = Event.CLEAR
        )
    }

    private fun ping() {
        synchronized(this) {
            var pingPallet = next(pallet)
            while (true) {
                val route = pool[pingPallet]
                if (route != null) {
                    getRoute(route.name)
                    Timer().schedule(1_000) {
                        synchronized(this) {
                            getBuses(route.name)
                        }
                    }
                    pallet = pingPallet
                    break
                } else {
                    pingPallet = next(pingPallet)
                }
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