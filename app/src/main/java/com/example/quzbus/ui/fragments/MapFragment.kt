package com.example.quzbus.ui.fragments

import android.content.Context
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
import com.example.quzbus.ui.adapters.SelectBusAdapter
import com.example.quzbus.ui.adapters.SelectCityAdapter
import com.example.quzbus.ui.viewmodels.MapViewModel
import com.example.quzbus.utils.NetworkResult
import com.example.quzbus.utils.afterTextChanged
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createCircleAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager
import com.redmadrobot.inputmask.MaskedTextChangedListener
import dagger.hilt.android.AndroidEntryPoint

//var mapView: MapView? = null

@AndroidEntryPoint
class MapFragment : Fragment() {

    private lateinit var mapView: MapView

    private val binding: FragmentMapBinding by viewBinding()
    private val selectCityAdapter by lazy { SelectCityAdapter(requireContext()) }
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

        mapView = binding.mapView
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS)

        val annotationApi = mapView.annotations

        //Draw line
        val polylineAnnotationManager = annotationApi.createPolylineAnnotationManager()

        //Draw circle
        val circleAnnotationManager = annotationApi.createCircleAnnotationManager()

        //Draw point
        val pointAnnotationManager = annotationApi.createPointAnnotationManager()

        val points = listOf(
            Point.fromLngLat(69.10, 54.85),
            Point.fromLngLat(69.15, 54.89)
        )

        val polylineAnnotationOptions = PolylineAnnotationOptions()
            .withPoints(points)
            .withLineColor(ContextCompat.getColor(requireContext(), R.color.orange_500))
            .withLineWidth(R.dimen.bus_5.toDouble())

        polylineAnnotationManager.create(polylineAnnotationOptions)

        isShowAuthField()
        addPhoneNumberMask()
        setupListeners()
        setupRecyclerViewSelectBus()
        setupRecyclerViewSelectCity()
        getCities()
        observeCities()
        checkPhoneNumberCodeDataChanged()
        setSmsCode()
    }

    private fun getSingleRoute() {
        selectBusAdapter.setOnItemClickListener {
            viewModel.getSingleRoute(it.route)
        }
    }

    private fun observeSingleRote() {
        viewModel.getSingleRouteResponse.observe(viewLifecycleOwner) {
            //TODO
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
        getSingleRoute()
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
            layoutManager = GridLayoutManager(requireContext(), 4)
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
            observeRoutes()
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
                    Toast.makeText(requireContext(), "LOADING", Toast.LENGTH_SHORT).show()
                }
                is NetworkResult.Success -> {
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
        viewModel.getRoutesResponse.observe(viewLifecycleOwner) { result ->

            when(result) {
                is NetworkResult.Loading -> {
                    binding.selectCity.root.visibility = View.GONE
                    binding.progressBar.visibility = View.VISIBLE
                    binding.selectBus.root.visibility = View.VISIBLE
                }
                is NetworkResult.Success -> {
                    val data = result.data?.routes
                    if (data != null ) {
                        result.data.let { selectBusAdapter.setNewData(it) }
                        binding.progressBar.visibility = View.GONE
                    }
                }
                else -> {
                    Toast.makeText(requireContext(), "ERROR", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


}