package com.example.quzbus.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.quzbus.data.models.response.Region
import com.example.quzbus.databinding.ItemCityBinding

class SelectCityAdapter(
    private val cities: List<Region>
) : RecyclerView.Adapter<SelectCityAdapter.SelectCityViewHolder>() {

    inner class SelectCityViewHolder(private val binding: ItemCityBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(region: Region) {
            binding.tvCityTitle.text = region.city
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectCityViewHolder {
        val binding =ItemCityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SelectCityViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SelectCityViewHolder, position: Int) {
        val current = cities[position]
        holder.bind(current)
    }

    override fun getItemCount(): Int {
        return cities.size
    }
}