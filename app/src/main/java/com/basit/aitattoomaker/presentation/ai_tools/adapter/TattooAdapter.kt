package com.basit.aitattoomaker.presentation.ai_tools.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.basit.aitattoomaker.databinding.ItemTattooBinding
import com.basit.aitattoomaker.presentation.ai_tools.model.Tattoo
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class TattooAdapter(
    private val onItemClick: (Tattoo) -> Unit
) : ListAdapter<Tattoo, TattooAdapter.TattooVH>(DIFF) {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        // Stable ID avoids weird blink/jumps; hash both fields
        val item = getItem(position)
        return (31 * item.name.hashCode() + item.tattooId).toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TattooVH {
        val binding = ItemTattooBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TattooVH(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: TattooVH, position: Int) {
        holder.bind(getItem(position))
    }

    class TattooVH(
        private val binding: ItemTattooBinding,
        private val onItemClick: (Tattoo) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Tattoo) = with(binding) {
            tvTattooName.text = item.name

            // Load drawable Int safely with Glide (handles vectors & caching)
            Glide.with(ivTattoo)
                .load(item.tattooId)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(ivTattoo)
            root.setOnClickListener { onItemClick(item) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Tattoo>() {
            override fun areItemsTheSame(old: Tattoo, new: Tattoo): Boolean {
                // If names are unique, this is fine; else include drawableRes
                return old.name == new.name
            }
            override fun areContentsTheSame(old: Tattoo, new: Tattoo): Boolean {
                return old == new
            }
        }
    }
}