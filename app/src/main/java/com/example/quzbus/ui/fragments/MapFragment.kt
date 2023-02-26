package com.example.quzbus.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quzbus.R
import com.example.quzbus.databinding.FragmentMapBinding
import com.example.quzbus.domain.models.routes.Direction
import com.example.quzbus.domain.models.routes.Pallet
import com.example.quzbus.domain.models.routes.Route
import com.example.quzbus.ui.adapters.ConsoleAdapter
import com.example.quzbus.ui.adapters.SelectBusAdapter
import com.example.quzbus.ui.adapters.SelectCityAdapter
import com.example.quzbus.ui.viewmodels.Event
import com.example.quzbus.ui.viewmodels.MapViewModel
import com.example.quzbus.utils.NetworkResult
import com.example.quzbus.utils.afterTextChanged
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.animation.CameraAnimatorOptions.Companion.cameraAnimatorOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.annotation.AnnotationPlugin
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.*
import com.redmadrobot.inputmask.MaskedTextChangedListener
import dagger.hilt.android.AndroidEntryPoint
import kotlin.collections.HashMap

@AndroidEntryPoint
class MapFragment : Fragment() {

    private lateinit var mapView: MapView
    private lateinit var annotationApi: AnnotationPlugin
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val permissionId = 2

    private val binding: FragmentMapBinding by viewBinding()
    private val selectCityAdapter by lazy { SelectCityAdapter() }
    private val selectBusAdapter by lazy { SelectBusAdapter() }
    private val consoleAdapter by lazy { ConsoleAdapter() }
    private val viewModel: MapViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        mapView = binding.mapView
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS)
        annotationApi = mapView.annotations

        observeSms()
        observeSmsCode()
        observeSheetState()
        observeRouteState()
        setupAnnotationManager(annotationApi)
        addPhoneNumberMask()
        setupListeners()
        setupRecyclerViewSelectBus()
        setupRecyclerViewSelectCity()
        setupRecyclerViewConsole()
        checkPhoneNumberCodeDataChanged()
        setupViewModel()

        showUserCity()
        getLocation()

    }

    private fun mapViewAnimation() {
        mapView.camera.apply {
            val bearing = createBearingAnimator(cameraAnimatorOptions(-45.0)) {
                duration = 4000
                interpolator = AccelerateDecelerateInterpolator()
            }
            val zoom = createZoomAnimator(
                cameraAnimatorOptions(14.0) {
                    startValue(3.0)
                }
            ) {
                duration = 4000
                interpolator = AccelerateDecelerateInterpolator()
            }
            playAnimatorsSequentially(zoom, bearing)
        }
    }

    private fun showUserCity() {
        val city = viewModel.getCity()
        if (city != null) cameraPosition(cityCoordinates(city))
    }

    private fun cityCoordinates(city: String): Point {
        return when (city) {
            "Астана" -> Point.fromLngLat(71.45, 51.18)
            "Алматы" -> Point.fromLngLat(79.93, 43.26)
            "Актау" -> Point.fromLngLat(51.17, 43.65)
            "Актобе" -> Point.fromLngLat(57.21, 50.28)
            "Атырау" -> Point.fromLngLat(51.88, 47.12)
            "Каскелен" -> Point.fromLngLat(71.45, 51.18)//todo
            "Костанай" -> Point.fromLngLat(63.62, 53.21)
            "Кызылорда" -> Point.fromLngLat(65.51, 44.85)
            "Петропавловск" -> Point.fromLngLat(69.15, 54.87)
            "Талгар" -> Point.fromLngLat(71.45, 51.18)//todo
            "Талдыкорган" -> Point.fromLngLat(78.37, 45.02)
            "Туркестан" -> Point.fromLngLat(68.25, 43.30)
            "Усть-каменогорск" -> Point.fromLngLat(82.61, 49.97)
            else -> Point.fromLngLat(0.1,0.1)
        }
    }

    private fun cameraPosition(point: Point?) {
        val cameraPosition = CameraOptions.Builder()
            .center(point)
            .zoom(11.0)
            .build()

        mapView.getMapboxMap().setCamera(cameraPosition)
        mapViewAnimation()
    }


    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
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
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            permissionId
        )
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == permissionId) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLocation()
            }
        }
    }

    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                fusedLocationClient.lastLocation.addOnCompleteListener(requireActivity()) { task ->
                    val location: Location? = task.result
                    if (location != null) {
                        val lat = location.latitude
                        val long = location.longitude
                        drawUserLocation(lat,long)
                        Log.d("TAG", "$lat + $long")
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Please turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    private fun setupAnnotationManager(annotationApi: AnnotationPlugin) {
        Pallet.values().forEach { pallet ->
            lines[pallet] = annotationApi.createPolylineAnnotationManager()
            circles[pallet] = annotationApi.createCircleAnnotationManager()
            points[pallet] = annotationApi.createPointAnnotationManager()
        }
    }

    private fun setupViewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.setup()
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
                    //TODO
                }
            }
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

    private fun observeSheetState() {
        viewModel.sheetState.observe(viewLifecycleOwner) {
            binding.authField.root.visibility = if (it.isAuthorized) View.GONE else View.VISIBLE
            binding.selectCity.root.visibility = if (it.isCitySelected) View.GONE else View.VISIBLE
            binding.selectBus.root.visibility = if (!it.isCitySelected) View.GONE else View.VISIBLE

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
            val list = mutableListOf<Route>()
            list.add(result.route)
            consoleAdapter.setNewData(list)

            when(result.event) {
                Event.CLEAR -> {
                    drawBuses(emptyList(), busPallet)
                    drawRoute(emptyList(), busPallet)
                    drawFlags(null, busPallet)
                }
                Event.BUS -> {
                    val directionBuses = busRoute.busPoints.filter { it.direction == busRoute.selectedDirection }
                    val busPoints = directionBuses.map { Point.fromLngLat(it.pointA.x, it.pointA.y) }
                    drawBuses(busPoints, busPallet)
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
                }
                Event.REDRAW -> {
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

                    val directionBuses = busRoute.busPoints.filter { it.direction == busRoute.selectedDirection }
                    val busPoints = directionBuses.map { Point.fromLngLat(it.pointA.x, it.pointA.y) }
                    drawBuses(busPoints, busPallet)
                }
            }
        }
    }

    private var lines = HashMap<Pallet, PolylineAnnotationManager>()
    private var circles = HashMap<Pallet, CircleAnnotationManager>()
    private var points = HashMap<Pallet, PointAnnotationManager>()

    //Выбор маршрута, в случае заполненности паллетки на 6 маршрутов - сообщение.
    private fun observeSelectedRoute() {
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

    private fun observeFavoriteRoute() {
        selectBusAdapter.setOnLongClickListener {
            viewModel.favoriteRoute(it.name)
        }
    }


    //Добавление маски номера телефона в форму для авторизации
    private fun addPhoneNumberMask() {
        val editText = binding.authField.etPhoneNumberEdit
        val listener = MaskedTextChangedListener("+ [0] ([000]) [000]-[00]-[00]", editText)

        editText.addTextChangedListener(listener)
        editText.onFocusChangeListener = listener
    }

    //Настройка слушателей
    private fun setupListeners() {
        listenSmsCode()
        listenSelectedRegion()
        observeSelectedRoute()
        showSettings()
        observeFavoriteRoute()
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

    private fun setupRecyclerViewConsole() {
        binding.rvBusConsole.apply {
            adapter = consoleAdapter
            layoutManager = LinearLayoutManager(requireContext())
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

    private fun showSettings() {
        binding.fabSettings.setOnClickListener { view: View ->
            showMenu(view, R.menu.menu_settings)
        }
    }

    private fun showMenu(view: View, @MenuRes menuRes: Int) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(menuRes, popup.menu)

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when(menuItem.itemId) {
                R.id.option_1 -> {
                    showDialogChangeCity()
                } else -> {
                    showDialogChangePhone()
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

    //При введении последней цифра СМС-кода - авторизация
    private fun observeSmsCode() {
        binding.authField.etSmsCodeEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 6) {
                    viewModel.getAuth(
                        binding.authField.etPhoneNumberEdit.text.toString().replace("[^0-9]".toRegex(), ""),
                        binding.authField.etSmsCodeEdit.text.toString()
                    )
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
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

    private fun clearMap(){
        lines.values.forEach { it.deleteAll() }
        circles.values.forEach { it.deleteAll() }
        points.values.forEach { it.deleteAll() }
    }

    //Отрисовка автобусов
    private fun drawBuses(points: List<Point>, pallet: Pallet) {
        val color = colorFor(pallet)
        val circleAnnotationManager = circles[pallet]
        circleAnnotationManager?.deleteAll()

        if (points.isEmpty()) return

        val options = mutableListOf<CircleAnnotationOptions>()
        for (point in points) {
            val option = CircleAnnotationOptions()
                .withPoint(point)
                .withCircleColor(ContextCompat.getColor(requireContext(), color))
                .withCircleRadius(8.0)

            options.add(option)
        }
        circleAnnotationManager?.create(options)
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
                .withLineWidth(5.0)
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

    private fun drawUserLocation(long: Double?, lang: Double?) {
        val pointAnnotationManager = annotationApi.createPointAnnotationManager()
        val userLong = long ?: 0.0
        val userLang = lang ?: 0.0

        val bitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.user_location)

        val options = PointAnnotationOptions()
            .withPoint(Point.fromLngLat(userLong, userLang))
            .withIconImage(bitmap)
            .withIconSize(2.0)

        pointAnnotationManager.create(options)

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
}