package com.revolage.quzbus.ui.fragments

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.viewbinding.library.fragment.viewBinding
import android.widget.Toast
import androidx.annotation.MenuRes
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.revolage.quzbus.R
import com.revolage.quzbus.databinding.FragmentMapBinding
import com.revolage.quzbus.domain.models.routes.Direction
import com.revolage.quzbus.domain.models.routes.Pallet
import com.revolage.quzbus.ui.adapters.SelectBusAdapter
import com.revolage.quzbus.ui.adapters.SelectCityAdapter
import com.revolage.quzbus.ui.viewmodels.Event
import com.revolage.quzbus.ui.viewmodels.MapViewModel
import com.revolage.quzbus.utils.NetworkResult
import com.revolage.quzbus.utils.afterTextChanged
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.observable.eventdata.CameraChangedEventData
import com.mapbox.maps.plugin.animation.CameraAnimatorOptions.Companion.cameraAnimatorOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.annotation.AnnotationPlugin
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.*
import com.mapbox.maps.plugin.delegates.listeners.OnCameraChangeListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.redmadrobot.inputmask.MaskedTextChangedListener
import com.revolage.quzbus.utils.hideKeyboard
import com.revolage.quzbus.utils.showKeyboard
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MapFragment : Fragment() {

    private lateinit var mapView: MapView
    private lateinit var annotationApi: AnnotationPlugin

    private val binding: FragmentMapBinding by viewBinding()
    private val selectCityAdapter by lazy { SelectCityAdapter() }
    private val selectBusAdapter by lazy { SelectBusAdapter(requireContext()) }
    private val viewModel: MapViewModel by viewModels()
    private val lines = hashMapOf<Pallet, PolylineAnnotationManager>()
    private val points = hashMapOf<Pallet, PointAnnotationManager>()
    private val pointsBuses = hashMapOf<Pallet, PointAnnotationManager>()
    private val busStops = hashMapOf<Pallet, PointAnnotationManager>()

    private val permissionId = 2

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = binding.mapView
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS)
        {
            mapView.location.updateSettings {
                enabled = true
                pulsingEnabled = true
            }
        }
        annotationApi = mapView.annotations

        viewModel.isLoading.observe(viewLifecycleOwner) {
            with(binding) {
                loadingBar.isVisible = it
                mapView.isVisible = !it
                standardBottomSheet.isVisible = !it
            }
        }

        observeAuth()
        observeSms()
        observeSmsFieldEdit()
        observePhoneFieldEdit()
        observeSheetState()
        observeRouteState()
        setupViewModel()
        setupAnnotationManager(annotationApi)
        setupListeners()
        setupRecyclerViewSelectCity()
        setupRecyclerViewSelectBus()
        addPhoneNumberMask()
        checkPhoneNumberCodeDataChanged()
        showUserCity()
        getLocation()
        listenCameraChange()
    }

    // region Observers
    private fun observeAuth() {
        viewModel.authResponse.observe(viewLifecycleOwner) {
            when(it) {
                is NetworkResult.Success -> {
                    viewModel.refreshAuth()
                }
                else -> {
                    // TODO
                }
            }
        }
    }

    private fun observeSms() {
        viewModel.getSmsCodeResponse.observe(viewLifecycleOwner) {
            when(it) {
                is NetworkResult.Success -> {
                    if (it.data?.result != "1") {
                        showSmsErrorReceive()
                    }
                }
                else -> {
                    // TODO
                }
            }
        }
    }

    //При введении последней цифра СМС-кода - авторизация
    private fun observeSmsFieldEdit() {
        binding.authField.etSmsCodeEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 6) {
                    viewModel.getAuth(
                        binding.authField.etPhoneNumberEdit.text.toString().replace("[^0-9]".toRegex(), ""),
                        binding.authField.etSmsCodeEdit.text.toString()
                    )
                    hideKeyboard()
                } else {
                    showKeyboard()
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun observePhoneFieldEdit() {
        binding.authField.etPhoneNumberEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 16) {
                    hideKeyboard()
                } else {
                    showKeyboard()
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun observeSheetState() {
        viewModel.sheetState.observe(viewLifecycleOwner) {
            binding.authField.root.visibility = if (it.isAuthorized) View.GONE else View.VISIBLE
            binding.selectCity.root.visibility = if (it.isAuthorized && !it.isCitySelected) View.VISIBLE else View.GONE
            binding.selectBus.root.visibility = if (!it.isCitySelected) View.GONE else View.VISIBLE
            binding.fabSettings.visibility = if (it.isCitySelected || it.isAuthorized) View.VISIBLE else View.GONE

            if (it.isCitySelected) {
                if (it.isAuthorized) {
                    selectBusAdapter.setNewData(it.routes)
                    binding.selectBus.tvSelectBusRouteTitle.text = getString(R.string.select_route)
                } else {
                    binding.selectBus.tvSelectBusRouteTitle.text = getString(R.string.select_route_error)
                }
            } else {
                selectCityAdapter.setNewData(it.cities)
            }
            if (it.error != null) {
                Toast.makeText(requireContext(), it.error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeRouteState() {
        viewModel.routeState.observe(viewLifecycleOwner) { result ->
            val busRoute = result.route ?: return@observe
            val busPallet = result.pallet ?: return@observe
            visibilityText()

            when(result.event) {
                Event.CLEAR -> {
                    drawBuses(emptyList(), emptyList() ,busPallet)
                    drawRoute(emptyList(), busPallet)
                    drawFlags(null, busPallet)
                    drawStops(emptyList(), busPallet)
                }
                Event.BUS -> {
                    val directionBuses = busRoute.busPoints.filter { it.direction == busRoute.selectedDirection }
                    val busPointsA = directionBuses.map { Point.fromLngLat(it.pointA.x, it.pointA.y) }
                    val busPointsB = directionBuses.map { Point.fromLngLat(it.pointB.x, it.pointB.y) }
                    drawBuses(busPointsB, busPointsA, busPallet)
                }
                Event.ROUTE -> {
                    val routePoints = if (busRoute.selectedDirection == Direction.DIRECTION_A) busRoute.routeA else busRoute.routeB
                    val points = routePoints.map { Point.fromLngLat(it.x, it.y) }

                    try {
                        val routeFinish = if (busRoute.selectedDirection == Direction.DIRECTION_A) busRoute.routeA.last() else busRoute.routeB.last()
                        val flagPoint = Point.fromLngLat(routeFinish.x, routeFinish.y)
                        drawFlags(flagPoint, busPallet)
                    } catch (e:Exception) {
                        Log.d("TAG", "${e.message}")
                    }

                    drawRoute(points, busPallet)

                    if (result.showStops) {
                        val stops = if (busRoute.selectedDirection == Direction.DIRECTION_A) busRoute.routeStopsA else busRoute.routeStopsB
                        val stopsCord = stops.map { Point.fromLngLat(it.point.x, it.point.y) }

                        drawStops(stopsCord, busPallet)
                    } else {
                        drawStops(emptyList(), busPallet)
                    }
                }
                Event.REDRAW -> {
                    val routePoints = if (busRoute.selectedDirection == Direction.DIRECTION_A) busRoute.routeA else busRoute.routeB
                    val points = routePoints.map { Point.fromLngLat(it.x, it.y) }

                    try {
                        val routeFinish = if (busRoute.selectedDirection == Direction.DIRECTION_A) busRoute.routeA.last() else busRoute.routeB.last()
                        val flagPoint = Point.fromLngLat(routeFinish.x, routeFinish.y)
                        drawFlags(flagPoint, busPallet)
                    } catch (e:Exception) {
                    }

                    drawRoute(points, busPallet)

                    if (result.showStops) {
                        val stops = if (busRoute.selectedDirection == Direction.DIRECTION_A) busRoute.routeStopsA else busRoute.routeStopsB
                        val stopsCord = stops.map { Point.fromLngLat(it.point.x, it.point.y) }

                        drawStops(stopsCord, busPallet)
                    } else {
                        drawStops(emptyList(), busPallet)
                    }

                    val directionBuses = busRoute.busPoints.filter { it.direction == busRoute.selectedDirection }
                    val busPointsA = directionBuses.map { Point.fromLngLat(it.pointA.x, it.pointA.y) }
                    val busPointsB = directionBuses.map { Point.fromLngLat(it.pointB.x, it.pointB.y) }
                    drawBuses(busPointsB, busPointsA ,busPallet)
                }
                Event.ZOOM -> {
                    if (result.showStops) {
                        val stops = if (busRoute.selectedDirection == Direction.DIRECTION_A) busRoute.routeStopsA else busRoute.routeStopsB
                        val stopsCord = stops.map { Point.fromLngLat(it.point.x, it.point.y) }

                        drawStops(stopsCord, busPallet)
                    } else {
                        drawStops(emptyList(), busPallet)
                    }
                }
            }
        }
    }

    // endregion Observers

    // region Setups
    private fun setupViewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.refreshAuth()
        }
    }

    private fun setupAnnotationManager(annotationApi: AnnotationPlugin) {
        Pallet.values().forEach { pallet ->
            lines[pallet] = annotationApi.createPolylineAnnotationManager()
            points[pallet] = annotationApi.createPointAnnotationManager()
            pointsBuses[pallet] = annotationApi.createPointAnnotationManager()
            busStops[pallet] = annotationApi.createPointAnnotationManager()
        }
    }

    //Настройка слушателей
    private fun setupListeners() {
        listenSmsCode()
        listenSelectedRegion()
        listenSelectedRoute()
        listenFavoriteRoute()
        showSettings()
    }

    //Настройка ресайклера для выбора города
    private fun setupRecyclerViewSelectCity() {
        binding.selectCity.rvSelectCities.apply {
            adapter = selectCityAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    //Настройка ресайклера для выбора автобуса
    private fun setupRecyclerViewSelectBus() {
        binding.selectBus.rvSelectBus.apply {
            adapter = selectBusAdapter
            layoutManager = GridLayoutManager(requireContext(), 5)
        }
    }

    // endregion Setups

    // region Helpers Methods(Phone Mask & SMS-field)
    //Добавление маски номера телефона в форму для авторизации
    private fun addPhoneNumberMask() {
        val editText = binding.authField.etPhoneNumberEdit
        val listener = MaskedTextChangedListener("+ 7 ([000]) [000]-[00]-[00]", editText)

        editText.addTextChangedListener(listener)
        editText.onFocusChangeListener = listener
    }

    //Проверка на смену номера телефона
    private fun checkPhoneNumberCodeDataChanged() {
        binding.authField.etSmsCodeEdit.apply {
            afterTextChanged {
                viewModel.authDataChanged(
                    binding.authField.etPhoneNumberEdit.text.toString().replace("[^0-9]".toRegex(), ""),
                    binding.authField.etSmsCodeEdit.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        viewModel.getAuth(
                            binding.authField.etPhoneNumberEdit.text.toString().replace("[^0-9]".toRegex(), ""),
                            binding.authField.etSmsCodeEdit.text.toString()
                        )
                }
                false
            }
        }
    }

    private fun visibilityText() {
        binding.selectBus.tvSelectBusRouteTitle.visibility =  if (viewModel.checkPool()) View.VISIBLE else View.GONE
    }
    // endregion Helpers Methods(Phone Mask & SMS-field)

    // region Map Draw & helper methods for Map
    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocation() {
        if (checkPermissions()) {
            showUserLocation()
        } else {
            requestPermissions()
        }
    }

    private fun showUserCity() {
        val city = viewModel.getCity()
        if (city != null) cameraPosition(cityCoordinates(city))
    }

    private fun clearMap(){
        lines.values.forEach { it.deleteAll() }
        points.values.forEach { it.deleteAll() }
        pointsBuses.values.forEach{ it.deleteAll() }
        busStops.values.forEach{ it.deleteAll() }
    }

    //Отрисовка остановок
    private fun drawStops(stops: List<Point>, pallet: Pallet) {
        val pointAnnotationManager = busStops[pallet]
        pointAnnotationManager?.deleteAll()
        if (stops.isEmpty()) return

        val bitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.icon_bus_stop)
        val pointsOptions = mutableListOf<PointAnnotationOptions>()
        for (stop in stops) {
            val options = PointAnnotationOptions()
                .withPoint(stop)
                .withIconImage(bitmap)
                .withIconSize(0.8)
            pointsOptions.add(options)
        }
        pointAnnotationManager?.create(pointsOptions)
    }

    //Отрисовка автобусов
    private fun drawBuses(pointsA: List<Point>, pointsB: List<Point>, pallet: Pallet) {
        val pointAnnotationManager = pointsBuses[pallet]
        pointAnnotationManager?.deleteAll()

        if (pointsA.isEmpty()) return

        val bitmap: Bitmap = BitmapFactory.decodeResource(resources,viewModel.getIconId())

        val pointsOptions = mutableListOf<PointAnnotationOptions>()
        for (index in pointsA.indices) {
            val pointStart = pointsA[index]
            val pointFinish = pointsB[index]

            val loc1 = android.location.Location("")
            loc1.latitude = pointStart.latitude()
            loc1.longitude = pointStart.longitude()

            val loc2 = android.location.Location("")
            loc2.latitude = pointFinish.latitude()
            loc2.longitude = pointFinish.longitude()

            val direction = loc1.bearingTo(loc2) + 90f

            val rotatedBitMap = rotateBitmap(bitmap, direction)

            val pointOption = PointAnnotationOptions()
                .withPoint(pointStart)
                .withIconImage(rotatedBitMap)
                .withIconSize(1.2)

            pointsOptions.add(pointOption)
        }
        pointAnnotationManager?.create(pointsOptions)
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    //Отрисовка маршрута
    private fun drawRoute(points: List<Point>, pallet: Pallet) {
        val color = colorFor(pallet)
        val polyLineAnnotationManager = lines[pallet]
        polyLineAnnotationManager?.deleteAll()

        val options = mutableListOf<PolylineAnnotationOptions>()
        for (point in points) {
            val option = PolylineAnnotationOptions()
                .withPoints(points)
                .withLineColor(ContextCompat.getColor(requireContext(), color))
                .withLineWidth(4.5)
            options.add(option)
        }
        polyLineAnnotationManager?.create(options)
    }

    //Отрисовка конечной точки маршрута
    private fun drawFlags(point: Point?, pallet: Pallet) {
        val pointAnnotationManager = points[pallet]
        pointAnnotationManager?.deleteAll()
        if (point == null) return

        val bitmap: Bitmap = BitmapFactory.decodeResource(resources,R.drawable.finish)

        val options = mutableListOf<PointAnnotationOptions>()
        val option = PointAnnotationOptions()
            .withPoint(point)
            .withIconImage(bitmap)
            .withIconSize(2.0)

        options.add(option)

        pointAnnotationManager?.create(options)
    }

    //Выбор цвета маршрута
    private fun colorFor(pallet: Pallet): Int {
        return when(pallet) {
            Pallet.RED -> R.color.red
            Pallet.GREEN -> R.color.green
            Pallet.BLUE -> R.color.blue
            Pallet.YELLOW -> R.color.yellow
            Pallet.PURPLE -> R.color.cyan
            Pallet.MAGENTA -> R.color.magenta
        }
    }

    private fun mapViewAnimation() {
        mapView.camera.apply {
            val zoom = createZoomAnimator(
                cameraAnimatorOptions(12.0) {
                    startValue(3.0)
                }
            ) {
                duration = 3000
                interpolator = AccelerateDecelerateInterpolator()
            }
            playAnimatorsSequentially(zoom)
        }
    }

    private fun showUserLocation() {
        mapView.location.updateSettings {
            enabled = true
            pulsingEnabled = true
        }
    }

    private fun cityCoordinates(city: String): Point {
        return when (city) {
            "Астана" -> Point.fromLngLat(71.447429, 51.168014)
            "Алматы" -> Point.fromLngLat(76.878096, 43.236352)
            "Актау" -> Point.fromLngLat(51.175112, 43.657283)
            "Актобе" -> Point.fromLngLat(57.171368, 50.283985)
            "Атырау" -> Point.fromLngLat(51.917083, 47.105050)
            "Аркалык" -> Point.fromLngLat(66.5441, 50.1455)
            "Есик" -> Point.fromLngLat(77.27, 43.21)
            "Каскелен" -> Point.fromLngLat(76.627544, 43.199252)
            "Костанай" -> Point.fromLngLat(63.640218, 53.212268)
            "Кызылорда" -> Point.fromLngLat(65.490363, 44.846396)
            "Петропавловск" -> Point.fromLngLat(69.149375, 54.863476)
            "Рудный" -> Point.fromLngLat(63.1168, 52.9729)
            "Талгар" -> Point.fromLngLat(77.240361, 43.302768)
            "Талдыкорган" -> Point.fromLngLat(78.380359, 45.015255)
            "Туркестан" -> Point.fromLngLat(68.239034, 43.304990)
            "Усть-Каменогорск" -> Point.fromLngLat(82.599988, 49.970557)
            "Шелек" -> Point.fromLngLat(78.1458, 43.3551)
            else -> Point.fromLngLat(0.1,0.1)
        }
    }

    private fun cameraPosition(point: Point?) {
        val cameraPosition = CameraOptions.Builder()
            .center(point)
            .zoom(8.0)
            .build()

        mapView.getMapboxMap().setCamera(cameraPosition)
        mapViewAnimation()
    }
    // endregion Map Draw & helper methods for Map

    // region Listeners
    private fun listenCameraChange() {
        val listener = OnCameraChangeListener { eventData: CameraChangedEventData ->
            val zoom = mapView.getMapboxMap().cameraState.zoom
            viewModel.setZoomLevel(zoom)
        }
        mapView.getMapboxMap().addOnCameraChangeListener(listener)
    }

    //Выбор маршрута, в случае заполненности паллетки на 6 маршрутов - сообщение.
    private fun listenSelectedRoute() {
        selectBusAdapter.setOnItemClickListener {
            val success = viewModel.selectRoute(it.name)
            if (!success) {
                Toast.makeText(
                    requireContext(),
                    "Выбрано максимальное количество маршрутов", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun listenFavoriteRoute() {
        selectBusAdapter.setOnLongClickListener {
            viewModel.favoriteRoute(it.name)
        }
    }

    //Получение СМС-кода
    private fun listenSmsCode() {
        binding.authField.btnSend.setOnClickListener {
            val phoneNumber = binding.authField.etPhoneNumberEdit.text.toString().replace("[^0-9]".toRegex(), "")
            viewModel.getSmsCode(phoneNumber)
        }
    }

    //Получение списка маршрутов, изменение видимости ресайклеров
    private fun listenSelectedRegion() {
        selectCityAdapter.setOnItemClickListener {
            viewModel.selectCity(it.city, it.rid)
            cameraPosition(cityCoordinates(it.city))
        }
    }
    // endregion Listeners

    // region Settings for reset City & Phone
    private fun showSettings() {
        binding.fabSettings.setOnClickListener { view: View ->
            showMenu(view, R.menu.menu_settings)
        }
    }

    private fun sendSms() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:")
            putExtra("address", "2505")
            putExtra("sms_body", "PAS")
        }
        startActivity(intent)
    }

    private fun showMenu(view: View, @MenuRes menuRes: Int) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(menuRes, popup.menu)

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when(menuItem.itemId) {
                R.id.option_1 -> {
                    showDialogChangeCity()
                } R.id.option_2 -> {
                    showDialogChangePhone()
                } else -> {
                    viewModel.setIconId()
                }
            }
            true
        }
        popup.setOnDismissListener {
            // Respond to popup being dismissed.
        }
        // Show the popup menu.
        popup.show()
    }

    private fun showDialogChangeCity() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.menu_city_title))
            .setMessage(resources.getString(R.string.menu_city_message))

            .setNegativeButton(resources.getString(R.string.menu_city_negative)) { _, _ ->
                return@setNegativeButton
            }
            .setPositiveButton(resources.getString(R.string.menu_city_positive)) { _, _ ->
                viewModel.resetCity()
                clearMap()
            }
            .show()
    }

    private fun showSmsErrorReceive() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.menu_sms_title))
            .setMessage(resources.getString(R.string.menu_sms_message))

            .setNegativeButton(resources.getString(R.string.menu_sms_negative)) { _, _ ->
                return@setNegativeButton
            }
            .setPositiveButton(resources.getString(R.string.menu_sms_positive)) { _, _ ->
                sendSms()
            }
            .show()
    }

    private fun showDialogChangePhone() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.menu_phone_title))
            .setMessage(resources.getString(R.string.menu_phone_message))

            .setNegativeButton(resources.getString(R.string.menu_phone_negative)) { _, _ ->
                return@setNegativeButton
            }
            .setPositiveButton(resources.getString(R.string.menu_phone_positive)) { _, _ ->
                viewModel.resetPhone()
            }
            .show()
    }
    // endregion Settings for reset City & Phone

    // region Request & Check Permissions
    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                ACCESS_COARSE_LOCATION,
                ACCESS_FINE_LOCATION
            ),
            permissionId
        )
    }
    // endregion Request & Check Permissions
}