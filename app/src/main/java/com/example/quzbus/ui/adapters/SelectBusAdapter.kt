package com.example.quzbus.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.quzbus.data.models.Bus
import com.example.quzbus.databinding.ItemCustomButtonBinding

class SelectBusAdapter(
    private val context: Context,
    private val buses: List<Bus>
) : RecyclerView.Adapter<SelectBusAdapter.SelectBusViewHolder>() {

    inner class SelectBusViewHolder(private val binding: ItemCustomButtonBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(bus: Bus) {
            binding.apply {
                tvBusNumber.text = context.getString(bus.number)
                tvBusOnline.text = bus.online.toString()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectBusViewHolder {
        val binding = ItemCustomButtonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SelectBusViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SelectBusViewHolder, position: Int) {
        val current = buses[position]
        holder.bind(current)
    }

    override fun getItemCount(): Int {
        return buses.size
    }
}