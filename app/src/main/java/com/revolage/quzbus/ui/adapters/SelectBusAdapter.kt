package com.revolage.quzbus.ui.adapters

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.revolage.quzbus.R
import com.revolage.quzbus.databinding.ItemCustomButtonBinding
import com.revolage.quzbus.domain.models.routes.Direction
import com.revolage.quzbus.domain.models.routes.Pallet
import com.revolage.quzbus.domain.models.routes.Route
import com.revolage.quzbus.utils.MyDiffUtil

class SelectBusAdapter(private val context: Context) : RecyclerView.Adapter<SelectBusAdapter.SelectBusViewHolder>() {

    private var busRoutes = emptyList<Route>()

    inner class SelectBusViewHolder(private val binding: ItemCustomButtonBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(route: Route) {
            binding.apply {
                val color: Int = when{
                    !route.isSelected && route.isFavorite -> Color.rgb(255, 215, 0)
                    route.isSelected && isNightThemeSelected(context) -> Color.WHITE
                    route.isSelected && !isNightThemeSelected(context) -> Color.WHITE
                    else -> Color.BLACK
                }
                tvBusNumber.setTextColor(color)
                tvBusNumber.text = route.name
                if (route.isSelected) {
                    route.pallet?.let { colorFor(it) }?.let { card.setCardBackgroundColor(it) }
                    if (route.selectedDirection != null) {
                        ivDirection.visibility = View.VISIBLE
                        if (route.selectedDirection == Direction.DIRECTION_A) {
                            ivDirection.setImageResource(R.drawable.arrow_up_white)
                        } else {
                            ivDirection.setImageResource(R.drawable.arrow_down_white)
                        }
                    } else {
                        ivDirection.visibility = View.GONE
                    }
                } else {
                    card.setCardBackgroundColor(Color.WHITE)
                    ivDirection.visibility = View.GONE
                }
                if (route.isFavorite) {
                    card.strokeColor = Color.rgb(255,215,0) //71, 74, 81
                    card.strokeWidth = 6
                } else {
                    card.strokeColor = Color.TRANSPARENT
                    card.strokeWidth = 0
                }
            }
            itemView.setOnClickListener {
                onItemClickListener?.let {
                    it(route)
                    notifyItemChanged(adapterPosition)
                }
            }
            itemView.setOnLongClickListener {
                onLongItemClickListener?.let {
                    it(route)
                    notifyItemChanged(adapterPosition)
                }
                true
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

    private var onLongItemClickListener:((Route) -> Unit)? = null
    fun setOnLongClickListener(listener: (Route) -> Unit) {
        onLongItemClickListener = listener
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

    fun isNightThemeSelected(context: Context): Boolean {
        val currentNightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }
}