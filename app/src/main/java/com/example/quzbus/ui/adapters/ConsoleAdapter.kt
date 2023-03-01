package com.example.quzbus.ui.adapters

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.quzbus.R
import com.example.quzbus.databinding.ItemConsoleBinding
import com.example.quzbus.domain.models.routes.Direction
import com.example.quzbus.domain.models.routes.Pallet
import com.example.quzbus.domain.models.routes.Route
import com.example.quzbus.utils.MyDiffUtil

class ConsoleAdapter : RecyclerView.Adapter<ConsoleAdapter.ConsoleViewHolder>() {

    private var busRoutes = emptyList<Route>()

    inner class ConsoleViewHolder(private val binding: ItemConsoleBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(route: Route) {
            binding.apply {
                    route.pallet?.let { colorFor(it) }?.let { tvBusDirectionFrom.setTextColor(it) }
                    route.pallet?.let { colorFor(it) }?.let { tvBusDirectionTo.setTextColor(it) }
                if (route.selectedDirection == Direction.DIRECTION_A) {
                    tvBusDirectionFrom.text = route.routeStart
                    tvBusDirectionTo.text = route.routeFinish
                    Log.d("TAG", "from DIR_A ${route.routeStart} ${route.routeFinish}")
                } else {
                    tvBusDirectionFrom.text = route.routeFinish
                    tvBusDirectionTo.text = route.routeStart
                    Log.d("TAG", "from DIR_B ${route.routeStart} ${route.routeFinish}")
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConsoleViewHolder {
        val binding = ItemConsoleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ConsoleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ConsoleViewHolder, position: Int) {
        val current = busRoutes[position]
        holder.bind(current)
    }

    override fun getItemCount(): Int {
        return busRoutes.size
    }

    fun setNewData(newData: List<Route>) {
        val diffUtil = MyDiffUtil(busRoutes, newData)
        val diffUtilResult = DiffUtil.calculateDiff(diffUtil)
        busRoutes = newData
        diffUtilResult.dispatchUpdatesTo(this)
    }

    private fun colorFor(pallet: Pallet): Int {
        return when(pallet) {
            Pallet.RED -> Color.RED
            Pallet.GREEN -> Color.GREEN
            Pallet.BLUE -> Color.BLUE
            Pallet.YELLOW -> Color.YELLOW
            Pallet.PURPLE -> Color.CYAN
            Pallet.MAGENTA -> Color.MAGENTA
        }
    }
}