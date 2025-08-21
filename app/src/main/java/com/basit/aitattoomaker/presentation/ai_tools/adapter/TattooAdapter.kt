package com.basit.aitattoomaker.presentation.ai_tools.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.basit.aitattoomaker.R
import com.basit.aitattoomaker.databinding.ItemTattooBinding
import com.basit.aitattoomaker.databinding.ItemTattooRoundBinding
import com.basit.aitattoomaker.presentation.ai_tools.model.Tattoo
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlin.math.abs

class TattooAdapter(
    private val onItemClick: (Tattoo) -> Unit
) : ListAdapter<Tattoo, TattooAdapter.TattooVH>(DIFF) {

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

            Glide.with(ivTattoo)
                .load(item.tattooId) // drawable @Res
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(ivTattoo)

            // 🔹 base scale/alpha (will be further animated by RecyclerView transformer)
            val targetScale = if (isSelected) 1.05f else 0.88f
            val targetAlpha = if (isSelected) 1f else 0.75f
            root.scaleX = targetScale
            root.scaleY = targetScale
            root.alpha  = targetAlpha

            // 🔹 optional: selected background stroke (use your selector/drawable)
            root.background = ContextCompat.getDrawable(
                root.context,
                if (isSelected) R.drawable.bg_oval /* gradient stroke */
                else R.drawable.bg_oval
            )

            root.setOnClickListener { onItemClick(item) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Tattoo>() {
            override fun areItemsTheSame(old: Tattoo, new: Tattoo) = old.name == new.name
            override fun areContentsTheSame(old: Tattoo, new: Tattoo) = old == new
        }
    }
}
