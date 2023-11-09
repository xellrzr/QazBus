package com.revolage.quzbus.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.revolage.quzbus.data.models.response.Region
import com.revolage.quzbus.databinding.ItemCityBinding
import com.revolage.quzbus.utils.MyDiffUtil

class SelectCityAdapter() : RecyclerView.Adapter<SelectCityAdapter.SelectCityViewHolder>() {

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
    }

    override fun getItemCount(): Int {
        return cities.size
    }

    fun setNewData(newData: List<Region>) {
        val diffUtil = MyDiffUtil(cities, newData)
        val diffUtilResult = DiffUtil.calculateDiff(diffUtil)
        cities = newData
        diffUtilResult.dispatchUpdatesTo(this)
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