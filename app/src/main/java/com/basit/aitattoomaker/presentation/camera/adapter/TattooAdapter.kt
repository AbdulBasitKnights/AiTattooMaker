package com.basit.aitattoomaker.presentation.camera.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.basit.aitattoomaker.R
import com.basit.aitattoomaker.databinding.ItemTattooBinding
import com.basit.aitattoomaker.domain.Tattoo
import com.bumptech.glide.Glide

class TattooAdapter(
    private val onClick: (Tattoo) -> Unit
) : ListAdapter<Tattoo, TattooAdapter.TattooViewHolder>(TattooDiffCallback()) {

    class TattooViewHolder(private val binding: ItemTattooBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(tattoo: Tattoo, onClick: (Tattoo) -> Unit) {
            Glide.with(binding.root)
                .load(R.drawable.dragon)
                .into(binding.ivTattoo)

            binding.root.setOnClickListener { onClick(tattoo) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TattooViewHolder {
        val binding = ItemTattooBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TattooViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TattooViewHolder, position: Int) {
        holder.bind(getItem(position), onClick)
    }

    class TattooDiffCallback : DiffUtil.ItemCallback<Tattoo>() {
        override fun areItemsTheSame(oldItem: Tattoo, newItem: Tattoo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Tattoo, newItem: Tattoo): Boolean {
            return oldItem == newItem
        }
    }
}