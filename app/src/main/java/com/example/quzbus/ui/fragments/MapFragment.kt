package com.example.quzbus.ui.fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.viewbinding.library.fragment.viewBinding
import android.widget.Toast
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
import com.example.quzbus.ui.adapters.SelectBusAdapter
import com.example.quzbus.ui.adapters.SelectCityAdapter
import com.example.quzbus.ui.viewmodels.MapViewModel
import com.example.quzbus.utils.NetworkResult
import com.example.quzbus.utils.afterTextChanged
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.*
import com.redmadrobot.inputmask.MaskedTextChangedListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MapFragment : Fragment() {

    private lateinit var mapView: MapView

    private val binding: FragmentMapBinding by viewBinding()
    private val selectCityAdapter by lazy { SelectCityAdapter(requireContext()) }
    private val selectBusAdapter by lazy { SelectBusAdapter() }
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

        mapView = binding.mapView
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS)

        val annotationApi = mapView.annotations

        for (pallet in Pallet.values()) {
            lines[pallet] = annotationApi.createPolylineAnnotationManager()
        }

        for (pallet in Pallet.values()) {
            circles[pallet] = annotationApi.createCircleAnnotationManager()
        }

        for (pallet in Pallet.values()) {
            points[pallet] = annotationApi.createPointAnnotationManager()
        }

        isShowAuthField()
        addPhoneNumberMask()
        setupListeners()
        setupRecyclerViewSelectBus()
        setupRecyclerViewSelectCity()
        getCities()
        observeCities()
        checkPhoneNumberCodeDataChanged()
        setSmsCode()
        observeRoutes()
    }

    private fun selectRoute() {
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

    private fun isShowAuthField() {
        binding.authField.root.visibility = if (viewModel.isUserLoggedIn()) View.GONE else View.VISIBLE
    }

    private fun addPhoneNumberMask() {
        val editText = binding.authField.etPhoneNumberEdit
        val listener = MaskedTextChangedListener("+ [0] ([000]) [000]-[00]-[00]", editText)

        editText.addTextChangedListener(listener)
        editText.onFocusChangeListener = listener
    }

    private fun setupListeners() {
        getSmsCode()
        selectRegion()
        selectRoute()
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

    private fun getCities() {
        lifecycleScope.launchWhenStarted {
            viewModel.getCities()
        }
    }

    private fun getSmsCode() {
        binding.authField.btnSend.setOnClickListener {
            val phoneNumber = binding.authField.etPhoneNumberEdit.text.toString().replace("[^0-9]".toRegex(), "")
            viewModel.getSmsCode(phoneNumber)
            Log.d("TAG", phoneNumber)
        }
    }

    private fun selectRegion() {
        selectCityAdapter.setOnItemClickListener {
            viewModel.getRoutes(it.rid)
            binding.selectCity.root.visibility = View.GONE
            binding.selectBus.root.visibility = View.VISIBLE
        }
    }

    private fun setSmsCode() {
        binding.authField.etSmsCodeEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 6) {
                    getAuth(
                        binding.authField.etPhoneNumberEdit.text.toString().replace("[^0-9]".toRegex(), ""),
                        binding.authField.etSmsCodeEdit.text.toString()
                    )
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun getAuth(phoneNumber: String, smsCode: String) {
        viewModel.getAuth(phoneNumber, smsCode)
        observeAuth()
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
                        getAuth(
                            binding.authField.etPhoneNumberEdit.text.toString().replace("[^0-9]".toRegex(), ""),
                            binding.authField.etSmsCodeEdit.text.toString()
                        )
                }
                false
            }
        }
    }

    private fun observeCities() {
        viewModel.getCitiesResponse.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    binding.pbMain.visibility = View.VISIBLE
                }
                is NetworkResult.Success -> {
                    binding.pbMain.visibility = View.GONE
                    val data = result.data?.regions
                    if (data != null) {
                        result.data.let { selectCityAdapter.setNewData(it) }
                    }
                }
                else -> {
                    Toast.makeText(requireContext(), "ERROR", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun observeAuth() {
        viewModel.getAuthResponse.observe(viewLifecycleOwner) { result ->
            when(result) {
                is NetworkResult.Loading -> {
                    Toast.makeText(requireContext(), "LOADING ROUTES", Toast.LENGTH_SHORT).show()
                }
                is NetworkResult.Success -> {
                    val data = result.data?.result
                    if (data != null) {
                        if (data.length > 1) binding.authField.root.visibility = View.GONE
                    }
                }
                else -> {
                    Toast.makeText(requireContext(), "ERROR", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun observeRoutes() {
        viewModel.routeState.observe(viewLifecycleOwner) { result ->
            val data = result.routes
            val busList = mutableListOf<Route>()
            for (route in data.values) {
                busList.add(route)
            }
            selectBusAdapter.setNewData(busList)

            for (route in data) {
                val busRoute = route.value
                if(busRoute.selectedDirection != null) {
                    val directionBuses = busRoute.busPoints.filter { it.direction == busRoute.selectedDirection }
                    val routePoints = if (busRoute.selectedDirection == Direction.DIRECTION_A) busRoute.routeA else busRoute.routeB
                    val points = routePoints.map { Point.fromLngLat(it.x, it.y) }
                    val busPoints = directionBuses.map { Point.fromLngLat(it.pointA.x, it.pointA.y) }
                    val routeFinish = if (busRoute.selectedDirection == Direction.DIRECTION_A) busRoute.routeA.last() else busRoute.routeB.last()
                    val flagPoint = Point.fromLngLat(routeFinish.x, routeFinish.y)
                    busRoute.pallet?.let {
                        drawBuses(busPoints, it)
                        drawRoute(points, it)
                        drawFlags(flagPoint, it)
                    }
                } else {
                    busRoute.pallet?.let {
                        drawBuses(emptyList(), it)
                        drawRoute(emptyList(), it)
                        drawFlags(null, it)
                    }
                }
            }
        }
    }

    private var lines = HashMap<Pallet, PolylineAnnotationManager>()
    private var circles = HashMap<Pallet, CircleAnnotationManager>()
    private var points = HashMap<Pallet, PointAnnotationManager>()

    private fun drawBuses(points: List<Point>, pallet: Pallet) {
        val color = colorFor(pallet)
        val circleAnnotationManager = circles[pallet]
        circleAnnotationManager?.deleteAll()

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

    private fun drawRoute(points: List<Point>, pallet: Pallet) {
        val color = colorFor(pallet)
        val polyLineAnnotationManager = lines[pallet]
        if (points.isEmpty()) {
            polyLineAnnotationManager?.deleteAll()
            return
        }

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

    private fun drawFlags(point: Point?, pallet: Pallet) {
        val pointAnnotationManager = points[pallet]
        if (point == null) {
            pointAnnotationManager?.deleteAll()
            return
        }
        val bitmap: Bitmap = BitmapFactory.decodeResource(resources,R.drawable.finish)

        val options = mutableListOf<PointAnnotationOptions>()
        val option = PointAnnotationOptions()
            .withPoint(point)
            .withIconImage(bitmap)
            .withIconSize(2.0)

        options.add(option)

        pointAnnotationManager?.create(options)
    }

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