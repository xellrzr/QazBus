package com.example.quzbus.ui.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.quzbus.R
import com.example.quzbus.data.models.response.Route
import com.example.quzbus.data.models.response.Routes
import com.example.quzbus.databinding.ItemCustomButtonBinding
import com.example.quzbus.utils.MyDiffUtil

class SelectBusAdapter(
    private val context: Context
) : RecyclerView.Adapter<SelectBusAdapter.SelectBusViewHolder>() {

    private var busRoutes = emptyList<Route>()
    private var singleItemSelection = - 1

    inner class SelectBusViewHolder(private val binding: ItemCustomButtonBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(route: Route) {
            binding.apply {
                tvBusNumber.text = route.route
                tvBusOnline.text = route.auto
            }
            itemView.setOnClickListener {
                onItemClickListener?.let {
                    it(route)
                    setSingleSelection(adapterPosition)
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
        setSelectedItemColor(position, holder)
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

    private fun setSelectedItemColor(
        position: Int,
        holder: SelectBusAdapter.SelectBusViewHolder
    ) {
        if (singleItemSelection == position) {
            holder.itemView.setBackgroundColor(context.resources.getColor(R.color.purple_500))
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    private fun setSingleSelection(adapterPosition: Int) {
        if (adapterPosition == RecyclerView.NO_POSITION) return

        notifyItemChanged(singleItemSelection)

        singleItemSelection = adapterPosition

        notifyItemChanged(singleItemSelection)
    }

    private var onItemClickListener:((Route) -> Unit)? = null
    fun setOnItemClickListener(listener: (Route) -> Unit) {
        onItemClickListener = listener
    }
}