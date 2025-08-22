package com.basit.aitattoomaker.presentation.ai_tools.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.basit.aitattoomaker.databinding.ItemTattooRoundBinding
import com.basit.aitattoomaker.presentation.ai_tools.model.Tattoo
import com.bumptech.glide.Glide

class TattooAdapterOld(
    private val onItemClick: (Tattoo) -> Unit
) : ListAdapter<Tattoo, TattooAdapterOld.TattooVH>(DIFF) {

    init { setHasStableIds(true) }

    // 🔹 track which item is "selected/centered"
    private var selectedPos: Int = RecyclerView.NO_POSITION

    override fun getItemId(position: Int): Long {
        val item = getItem(position)
        return (31L * item.name.hashCode() + item.tattooId)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TattooVH {
        val binding = ItemTattooRoundBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TattooVH(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: TattooVH, position: Int) {
        val isSelected = position == selectedPos
        holder.bind(getItem(position), isSelected)
    }

    // 🔹 call this from your Fragment when the centered item changes
    fun setSelected(newPos: Int) {
        val old = selectedPos
        if (newPos == old || newPos == RecyclerView.NO_POSITION) return
        selectedPos = newPos
        if (old != RecyclerView.NO_POSITION) notifyItemChanged(old)
        notifyItemChanged(newPos)
    }

    class TattooVH(
        private val binding: ItemTattooRoundBinding,
        private val onItemClick: (Tattoo) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Tattoo, isSelected: Boolean) = with(binding) {
            tvTattooName.text = item.name
            Glide.with(ivTattoo).load(item.tattooId).into(ivTattoo)
            root.setOnClickListener { onItemClick(item) }
            // base visual state; scale/alpha will also be adjusted by scroll listener
//            root.isSelected = isSelected
//            if (isSelected) {
//                focusRing.visibility=View.VISIBLE
//                onItemClick(item)
//            } else {focusRing.visibility=View.GONE}
        }

    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Tattoo>() {
            override fun areItemsTheSame(old: Tattoo, new: Tattoo) = old.name == new.name
            override fun areContentsTheSame(old: Tattoo, new: Tattoo) = old == new
        }
    }
}
