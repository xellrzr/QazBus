package com.example.quzbus.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.viewbinding.library.fragment.viewBinding
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quzbus.R
import com.example.quzbus.data.models.Bus
import com.example.quzbus.data.models.City
import com.example.quzbus.data.models.response.Region
import com.example.quzbus.databinding.FragmentMapBinding
import com.example.quzbus.ui.adapters.SelectBusAdapter
import com.example.quzbus.ui.adapters.SelectCityAdapter
import com.example.quzbus.ui.viewmodels.MapViewModel
import com.example.quzbus.utils.NetworkResult
import com.example.quzbus.utils.afterTextChanged
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import dagger.hilt.android.AndroidEntryPoint

var mapView: MapView? = null

@AndroidEntryPoint
class MapFragment : Fragment() {

    private val binding: FragmentMapBinding by viewBinding()
    private val data = loadCities()
    private val buses = loadBuses()
    private val selectCityAdapter by lazy { SelectCityAdapter(list) }
    private val selectBusAdapter by lazy { SelectBusAdapter(requireContext(), buses) }
    private val viewModel: MapViewModel by viewModels()
    private val list: MutableList<Region> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenStarted {
            viewModel.getCities()
            observeCities()
            setupRecyclerViewSelectCity()
        }

        mapView = binding.mapView
        mapView?.getMapboxMap()?.loadStyleUri(Style.MAPBOX_STREETS)

        setupRecyclerViewSelectBus()

        setupListeners()
        observeCities()
        setupRecyclerViewSelectCity()
        checkPhoneNumberCodeDataChanged()

    }

    private fun observeCities() {
        viewModel.getCitiesResponse.observe(viewLifecycleOwner) {
            val result = it

            when (result) {
                is NetworkResult.Success -> {
                    val data = result.data?.regions
                    if (data != null) {
                        list.addAll(data)
                    }
                }
                else -> {
                    Toast.makeText(requireContext(), "${result.data?.regions}", Toast.LENGTH_SHORT).show()
                    val data = result.data?.regions
                    if (data != null) {
                        list.addAll(data)
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        getSmsCode()
    }

    private fun getSmsCode() {
        binding.authField.btnSend.setOnClickListener {
            viewModel.getSmsCode(binding.authField.etPhoneNumberEdit.text.toString())
            Log.d("TAG", binding.authField.etPhoneNumberEdit.text.toString())
        }
    }

    private fun checkPhoneNumberCodeDataChanged() {
        binding.authField.etSmsCodeEdit.apply {
            afterTextChanged {
                viewModel.authDataChanged(
                    binding.authField.etPhoneNumberEdit.text.toString(),
                    binding.authField.etSmsCodeEdit.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        getAuth(
                            binding.authField.etPhoneNumberEdit.text.toString(),
                            "0",
                            binding.authField.etSmsCodeEdit.text.toString()
                        )
                }
                false
            }
        }
    }

    private fun getAuth(phoneNumber: String, language: String, smsCode: String) {
        viewModel.getAuth(phoneNumber, language, smsCode)
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

    companion object {
        fun loadCities(): List<City> {
            return listOf(
                City(R.string.city1),
                City(R.string.city2),
                City(R.string.city3),
                City(R.string.city4),
                City(R.string.city5),
                City(R.string.city6),
                City(R.string.city7),
                City(R.string.city8),
                City(R.string.city9),
                City(R.string.city10),
                City(R.string.city11),
                City(R.string.city12)
            )
        }

        fun loadBuses(): List<Bus> {
            return listOf(
                Bus(R.string.bus1, 2),
                Bus(R.string.bus2, 1),
                Bus(R.string.bus3, 0),
                Bus(R.string.bus4, 11),
                Bus(R.string.bus5, 2),
                Bus(R.string.bus6, 5),
                Bus(R.string.bus7, 7),
                Bus(R.string.bus8, 1),
                Bus(R.string.bus9, 2),
                Bus(R.string.bus10, 4),
                Bus(R.string.bus11, 3),
                Bus(R.string.bus12, 2),
            )
        }
    }
}