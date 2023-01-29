package com.example.quzbus.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.viewbinding.library.fragment.viewBinding
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quzbus.R
import com.example.quzbus.data.models.City
import com.example.quzbus.databinding.FragmentMapBinding
import com.example.quzbus.ui.adapters.SelectAdapter
import com.mapbox.maps.MapView
import com.mapbox.maps.Style

var mapView: MapView? = null

class MapFragment : Fragment() {

    private val binding: FragmentMapBinding by viewBinding()
    private val data = loadCities()
    private val selectAdapter by lazy { SelectAdapter(requireContext(),data) }

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
        mapView?.getMapboxMap()?.loadStyleUri(Style.MAPBOX_STREETS)

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        binding.rvCities.apply {
            adapter = selectAdapter
            layoutManager = LinearLayoutManager(requireContext())
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
    }
}