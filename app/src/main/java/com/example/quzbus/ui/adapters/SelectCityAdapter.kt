package com.example.quzbus.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.quzbus.data.models.City
import com.example.quzbus.databinding.ItemCityBinding

class SelectCityAdapter(
    private val context: Context,
    private val cities: List<City>
) : RecyclerView.Adapter<SelectCityAdapter.SelectCityViewHolder>() {

    inner class SelectCityViewHolder(private val binding: ItemCityBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(city: City) {
            binding.tvCityTitle.text = context.getString(city.title)
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