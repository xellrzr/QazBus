package com.example.quzbus.ui.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.quzbus.R
import com.example.quzbus.data.models.response.Message
import com.example.quzbus.data.models.response.Region
import com.example.quzbus.databinding.ItemCityBinding
import com.example.quzbus.utils.MyDiffUtil

class SelectCityAdapter(
    private val context: Context
) : RecyclerView.Adapter<SelectCityAdapter.SelectCityViewHolder>() {

    private var cities = emptyList<Region>()
    private var singleItemSelection = - 1

    inner class SelectCityViewHolder(private val binding: ItemCityBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(region: Region) {
            binding.tvCityTitle.text = region.city
            itemView.setOnClickListener {
                onItemClickListener?.let {
                    it(region)
                    setSingleSelection(adapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectCityViewHolder {
        val binding =ItemCityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SelectCityViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SelectCityViewHolder, position: Int) {
        val current = cities[position]
        holder.bind(current)
        setSelectedItemColor(position, holder)
    }

    override fun getItemCount(): Int {
        return cities.size
    }

    fun setNewData(newData: Message) {
        val diffUtil = MyDiffUtil(cities, newData.regions)
        val diffUtilResult = DiffUtil.calculateDiff(diffUtil)
        cities = newData.regions
        diffUtilResult.dispatchUpdatesTo(this)
    }

    private fun setSelectedItemColor(
        position: Int,
        holder: SelectCityViewHolder
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

    private var onItemClickListener:((Region) -> Unit)? = null
    fun setOnItemClickListener(listener: (Region) -> Unit) {
        onItemClickListener = listener
    }
}