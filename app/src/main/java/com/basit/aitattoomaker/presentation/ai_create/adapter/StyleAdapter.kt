package com.basit.aitattoomaker.presentation.ai_create.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.basit.aitattoomaker.databinding.ItemStyleBinding
import com.basit.aitattoomaker.presentation.ai_create.model.StyleItem
import com.bumptech.glide.Glide

class StyleAdapter(
    private val onClick: (StyleItem) -> Unit
) : ListAdapter<StyleItem, StyleAdapter.StyleViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<StyleItem>() {
        override fun areItemsTheSame(oldItem: StyleItem, newItem: StyleItem) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: StyleItem, newItem: StyleItem) = oldItem == newItem
    }

    inner class StyleViewHolder(private val binding: ItemStyleBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: StyleItem) {
            binding.tvStyle.text = item.title
            Glide.with(binding.imgStyle)
                .load(item.url)
                .into(binding.imgStyle)
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StyleViewHolder {
        val binding = ItemStyleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StyleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StyleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
