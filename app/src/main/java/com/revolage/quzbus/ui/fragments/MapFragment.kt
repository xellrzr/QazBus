package com.revolage.quzbus.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.viewbinding.library.fragment.viewBinding
import android.widget.Toast
import androidx.annotation.MenuRes
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.LocationServices
import com.revolage.quzbus.R
import com.revolage.quzbus.databinding.FragmentMapBinding
import com.revolage.quzbus.domain.models.routes.Direction
import com.revolage.quzbus.ui.adapters.SelectBusAdapter
import com.revolage.quzbus.ui.adapters.SelectCityAdapter
import com.revolage.quzbus.ui.viewmodels.Event
import com.revolage.quzbus.ui.viewmodels.MapViewModel
import com.revolage.quzbus.utils.NetworkResult
import com.revolage.quzbus.utils.afterTextChanged
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.AnnotationPlugin
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.scalebar.scalebar
import com.redmadrobot.inputmask.MaskedTextChangedListener
import com.revolage.quzbus.ui.annotation.AnnotationManager
import com.revolage.quzbus.ui.camera.CameraController
import com.revolage.quzbus.ui.coordinates.CityCoordinates.Companion.cityCoordinates
import com.revolage.quzbus.utils.Constants.Companion.PERMISSION_ID
import com.revolage.quzbus.utils.PermissionManager
import com.revolage.quzbus.domain.repository.NetworkConnectivityRepository
import com.revolage.quzbus.utils.hideKeyboard
import com.revolage.quzbus.utils.showKeyboard
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MapFragment : Fragment() {

    private lateinit var mapView: MapView
    private lateinit var annotationApi: AnnotationPlugin
    private lateinit var annotationManager: AnnotationManager
    private lateinit var cameraController: CameraController
    private lateinit var permissionManager: PermissionManager

    private val binding: FragmentMapBinding by viewBinding()
    private val selectCityAdapter by lazy { SelectCityAdapter() }
    private val selectBusAdapter by lazy { SelectBusAdapter(requireContext()) }
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

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.status.collect { status ->
                when (status) {
                    NetworkConnectivityRepository.Status.UNAVAILABLE,
                    NetworkConnectivityRepository.Status.LOSING,
                    NetworkConnectivityRepository.Status.LOST -> showSnackBarConnectionLost()
                    NetworkConnectivityRepository.Status.AVAILABLE -> viewModel.retryLastRequest()
                }
            }
        }

        mapView = binding.mapView
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS)
        {
            mapView.location.updateSettings {
                enabled = true
                pulsingEnabled = true
            }
        }
        annotationApi = mapView.annotations

        val scaleBarPlugin = mapView.scalebar
        scaleBarPlugin.enabled = false

        annotationManager = AnnotationManager(annotationApi, resources, requireContext()) { viewModel.getIconId() }
        cameraController = CameraController(mapView)
        permissionManager = PermissionManager(requireActivity())

        binding.ivMinus.setOnClickListener {
            cameraZoomMinus()
        }

        binding.ivPlus.setOnClickListener {
            cameraZoomPlus()
        }

        observeLoading()
        observeAuth()
        observeSms()
        observeSmsFieldEdit()
        observePhoneFieldEdit()
        observeSheetState()
        observeRouteState()
        setupViewModel()
        setupListeners()
        setupRecyclerViewSelectCity()
        setupRecyclerViewSelectBus()
        addPhoneNumberMask()
        checkPhoneNumberCodeDataChanged()
        showUserCity()
        checkPermissions()
        moveToUserLocation()
        cameraController.listenCameraChange { zoomLevel -> viewModel.setZoomLevel(zoomLevel) }
    }

    private fun observeLoading() {
        viewModel.isLoading.observe(viewLifecycleOwner) {
            with(binding) {
                loadingBar.isVisible = it
                standardBottomSheet.isVisible = !it
            }
        }
    }

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
            with(binding) {
                authField.root.visibility = if (it.isAuthorized) View.GONE else View.VISIBLE
                selectCity.root.visibility = if (it.isAuthorized && !it.isCitySelected) View.VISIBLE else View.GONE
                selectBus.root.visibility = if (!it.isCitySelected) View.GONE else View.VISIBLE
                fabSettings.visibility = if (it.isCitySelected || it.isAuthorized) View.VISIBLE else View.GONE

                if (it.isCitySelected) {
                    if (it.isAuthorized) {
                        selectBusAdapter.setNewData(it.routes)
                        selectBus.tvSelectBusRouteTitle.text = getString(R.string.select_route)
                    } else {
                        selectBus.tvSelectBusRouteTitle.text = getString(R.string.select_route_error)
                    }
                } else {
                    selectCityAdapter.setNewData(it.cities)
                }
                if (it.error != null) {
                    Snackbar.make(view, it.error, Snackbar.LENGTH_LONG).show()
                }
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
                    annotationManager.drawBuses(emptyList(), emptyList() ,busPallet)
                    annotationManager.drawRoute(emptyList(), busPallet)
                    annotationManager.drawFlags(null, busPallet)
                    annotationManager.drawStops(emptyList(), busPallet)
                }
                Event.BUS -> {
                    val directionBuses = busRoute.busPoints.filter { it.direction == busRoute.selectedDirection }
                    val busPointsA = directionBuses.map { Point.fromLngLat(it.pointA.x, it.pointA.y) }
                    val busPointsB = directionBuses.map { Point.fromLngLat(it.pointB.x, it.pointB.y) }
                    annotationManager.drawBuses(busPointsB, busPointsA, busPallet)
                }
                Event.ROUTE -> {
                    val routePoints = if (busRoute.selectedDirection == Direction.DIRECTION_A) busRoute.routeA else busRoute.routeB
                    val points = routePoints.map { Point.fromLngLat(it.x, it.y) }

                    try {
                        val routeFinish = if (busRoute.selectedDirection == Direction.DIRECTION_A) busRoute.routeA.last() else busRoute.routeB.last()
                        val flagPoint = Point.fromLngLat(routeFinish.x, routeFinish.y)
                        annotationManager.drawFlags(flagPoint, busPallet)
                    } catch (e:Exception) {
                        Log.d("TAG", "${e.message}")
                    }

                    annotationManager.drawRoute(points, busPallet)

                    if (result.showStops) {
                        val stops = if (busRoute.selectedDirection == Direction.DIRECTION_A) busRoute.routeStopsA else busRoute.routeStopsB
                        val stopsCord = stops.map { Point.fromLngLat(it.point.x, it.point.y) }

                        annotationManager.drawStops(stopsCord, busPallet)
                    } else {
                        annotationManager.drawStops(emptyList(), busPallet)
                    }
                }
                Event.REDRAW -> {
                    val routePoints = if (busRoute.selectedDirection == Direction.DIRECTION_A) busRoute.routeA else busRoute.routeB
                    val points = routePoints.map { Point.fromLngLat(it.x, it.y) }

                    try {
                        val routeFinish = if (busRoute.selectedDirection == Direction.DIRECTION_A) busRoute.routeA.last() else busRoute.routeB.last()
                        val flagPoint = Point.fromLngLat(routeFinish.x, routeFinish.y)
                        annotationManager.drawFlags(flagPoint, busPallet)
                    } catch (e:Exception) {
                        Log.d("TAG", "${e.message}")
                    }

                    annotationManager.drawRoute(points, busPallet)

                    if (result.showStops) {
                        val stops = if (busRoute.selectedDirection == Direction.DIRECTION_A) busRoute.routeStopsA else busRoute.routeStopsB
                        val stopsCord = stops.map { Point.fromLngLat(it.point.x, it.point.y) }

                        annotationManager.drawStops(stopsCord, busPallet)
                    } else {
                        annotationManager.drawStops(emptyList(), busPallet)
                    }

                    val directionBuses = busRoute.busPoints.filter { it.direction == busRoute.selectedDirection }
                    val busPointsA = directionBuses.map { Point.fromLngLat(it.pointA.x, it.pointA.y) }
                    val busPointsB = directionBuses.map { Point.fromLngLat(it.pointB.x, it.pointB.y) }
                    annotationManager.drawBuses(busPointsB, busPointsA ,busPallet)
                }
                Event.ZOOM -> {
                    if (result.showStops) {
                        val stops = if (busRoute.selectedDirection == Direction.DIRECTION_A) busRoute.routeStopsA else busRoute.routeStopsB
                        val stopsCord = stops.map { Point.fromLngLat(it.point.x, it.point.y) }

                        annotationManager.drawStops(stopsCord, busPallet)
                    } else {
                        annotationManager.drawStops(emptyList(), busPallet)
                    }
                }
            }
        }
    }

    private fun setupViewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.refreshAuth()
        }
    }

    private fun setupListeners() {
        listenSmsCode()
        listenSelectedRegion()
        listenSelectedRoute()
        listenFavoriteRoute()
        showSettings()
    }

    private fun setupRecyclerViewSelectCity() {
        binding.selectCity.rvSelectCities.apply {
            adapter = selectCityAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupRecyclerViewSelectBus() {
        binding.selectBus.rvSelectBus.apply {
            adapter = selectBusAdapter
            layoutManager = GridLayoutManager(requireContext(), 5)
        }
    }

    private fun addPhoneNumberMask() {
        val editText = binding.authField.etPhoneNumberEdit
        val listener = MaskedTextChangedListener("+ 7 ([000]) [000]-[00]-[00]", editText)

        editText.addTextChangedListener(listener)
        editText.onFocusChangeListener = listener
    }

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
        binding.selectBus.tvSelectBusRouteTitle.isVisible =  viewModel.checkPool()
    }

    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun checkPermissions() {
        if (permissionManager.checkPermissions()) {
            showUserLocation()
            binding.fabUserLocation.isVisible = true
        } else {
            permissionManager.requestPermissions()
            binding.fabUserLocation.isVisible = false
        }
    }

    @Deprecated("Deprecated in Java", ReplaceWith(
        "super.onRequestPermissionsResult(requestCode, permissions, grantResults)",
        "androidx.fragment.app.Fragment")
    )
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_ID -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    showUserLocation()
                    binding.fabUserLocation.isVisible = true
                } else {
                    binding.fabUserLocation.isVisible = false
                }
            }
        }
    }

    private fun showUserCity() {
        val city = viewModel.getCity()
        if (city != null) cameraController.cameraPosition(cityCoordinates(city))
    }

    private fun showUserLocation() {
        mapView.location.updateSettings {
            enabled = true
            pulsingEnabled = true
        }
    }

    private fun cameraZoomMinus() {
        cameraController.zoomMinus()
    }

    private fun cameraZoomPlus() {
        cameraController.zoomPlus()
    }

    @SuppressLint("MissingPermission")
    private fun moveToUserLocation() {
        binding.fabUserLocation.setOnClickListener {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        val userLat = location.latitude
                        val userLong = location.longitude
                        cameraController.moveToLocation(Point.fromLngLat(userLong, userLat))
                    }
                }
                .addOnFailureListener { e ->
                    // Обработай ошибку получения местоположения
                }
        }
    }

    private fun listenSelectedRoute() {
        selectBusAdapter.setOnItemClickListener {
            val success = viewModel.selectRoute(it.name)
            if (!success) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.routes_max_count), Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun listenFavoriteRoute() {
        selectBusAdapter.setOnLongClickListener {
            viewModel.favoriteRoute(it.name)
        }
    }

    private fun listenSmsCode() {
        binding.authField.btnSend.setOnClickListener {
            val phoneNumber = binding.authField.etPhoneNumberEdit.text.toString().replace("[^0-9]".toRegex(), "")
            viewModel.getSmsCode(phoneNumber)
        }
    }

    private fun listenSelectedRegion() {
        selectCityAdapter.setOnItemClickListener {
            viewModel.selectCity(it.city, it.rid)
            cameraController.cameraPosition(cityCoordinates(it.city))
        }
    }

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
                annotationManager.clearMap()
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

    private fun showSnackBarConnectionLost() {
        view?.let { Snackbar.make(it, getString(R.string.internet_connection_check), Snackbar.LENGTH_LONG).show() }
    }

}