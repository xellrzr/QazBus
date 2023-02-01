package com.example.quzbus.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.quzbus.data.models.response.Route
import com.example.quzbus.data.models.response.Routes
import com.example.quzbus.databinding.ItemCustomButtonBinding
import com.example.quzbus.utils.MyDiffUtil

class SelectBusAdapter(
) : RecyclerView.Adapter<SelectBusAdapter.SelectBusViewHolder>() {

    private var busRoutes = emptyList<Route>()

    inner class SelectBusViewHolder(private val binding: ItemCustomButtonBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(route: Route) {
            binding.apply {
                tvBusNumber.text = route.route
                tvBusOnline.text = route.auto
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectBusViewHolder {
        val binding = ItemCustomButtonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SelectBusViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SelectBusViewHolder, position: Int) {
        val current = busRoutes[position]
        holder.bind(current)
    }

    override fun getItemCount(): Int {
        return busRoutes.size
    }

    fun setNewData(newData: Routes) {
        val diffUtil = MyDiffUtil(busRoutes, newData.routes)
        val diffUtilResult = DiffUtil.calculateDiff(diffUtil)
        busRoutes = newData.routes
        diffUtilResult.dispatchUpdatesTo(this)
    }
}