package com.example.quzbus.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.quzbus.R
import com.example.quzbus.databinding.ItemCustomButtonBinding
import com.example.quzbus.domain.models.routes.Direction
import com.example.quzbus.domain.models.routes.Pallet
import com.example.quzbus.domain.models.routes.Route
import com.example.quzbus.utils.MyDiffUtil

class SelectBusAdapter : RecyclerView.Adapter<SelectBusAdapter.SelectBusViewHolder>() {

    private var busRoutes = emptyList<Route>()

    inner class SelectBusViewHolder(private val binding: ItemCustomButtonBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(route: Route) {
            binding.apply {
                tvBusNumber.text = route.name
                tvBusOnline.text = route.auto
                if (route.isSelected) {
                    route.pallet?.let { colorFor(it) }?.let { card.setCardBackgroundColor(it) }
                    if (route.selectedDirection != null) {
                        ivDirection.visibility = View.VISIBLE
                        if (route.selectedDirection == Direction.DIRECTION_A) {
                            ivDirection.setImageResource(R.drawable.arrow_up)
                        } else {
                            ivDirection.setImageResource(R.drawable.arrow_down)
                        }
                    } else {
                        ivDirection.visibility = View.GONE
                    }
                } else {
                    card.setCardBackgroundColor(Color.WHITE)
                    ivDirection.visibility = View.GONE
                }
            }
            itemView.setOnClickListener {
                onItemClickListener?.let {
                    it(route)
                    setMultipleSelection(adapterPosition)
                }
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

    fun setNewData(newData: List<Route>) {
        val diffUtil = MyDiffUtil(busRoutes, newData)
        val diffUtilResult = DiffUtil.calculateDiff(diffUtil)
        busRoutes = newData
        diffUtilResult.dispatchUpdatesTo(this)
    }

    private var onItemClickListener:((Route) -> Unit)? = null
    fun setOnItemClickListener(listener: (Route) -> Unit) {
        onItemClickListener = listener
    }

    private fun setMultipleSelection(adapterPosition: Int) {
        val route = busRoutes[adapterPosition]
        if (route.selectedDirection == null) {
            route.isSelected = false
        } else {
            busRoutes[adapterPosition].isSelected = true
        }
        notifyItemChanged(adapterPosition)
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