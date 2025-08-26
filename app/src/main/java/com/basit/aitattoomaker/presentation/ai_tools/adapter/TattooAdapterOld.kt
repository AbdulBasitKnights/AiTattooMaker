package com.basit.aitattoomaker.presentation.ai_tools.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.basit.aitattoomaker.databinding.ItemTattooRoundBinding
import com.basit.aitattoomaker.presentation.ai_tools.model.CameraTattoo
import com.bumptech.glide.Glide

class TattooAdapterOld(
    private val onItemClick: (CameraTattoo) -> Unit
) : ListAdapter<CameraTattoo, TattooAdapterOld.TattooVH>(DIFF) {

    init { setHasStableIds(true) }

    // 🔹 track which item is "selected/centered"
    private var selectedPos: Int = RecyclerView.NO_POSITION

    override fun getItemId(position: Int): Long {
        val item = getItem(position)
        return (31L * item.name.hashCode() + item.id)
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
        private val onItemClick: (CameraTattoo) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CameraTattoo, isSelected: Boolean) = with(binding) {
            tvTattooName.text = item.name
            Glide.with(ivTattoo).load(item.id).into(ivTattoo)
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
        private val DIFF = object : DiffUtil.ItemCallback<CameraTattoo>() {
            override fun areItemsTheSame(old: CameraTattoo, new: CameraTattoo) = old.name == new.name
            override fun areContentsTheSame(old: CameraTattoo, new: CameraTattoo) = old == new
        }
    }
}
