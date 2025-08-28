package com.basit.aitattoomaker.presentation.camera.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.basit.aitattoomaker.databinding.ItemTattooSquareBinding
import com.basit.aitattoomaker.presentation.ai_tools.model.CameraTattoo
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class CameraTattooAdapter(
    private val onClick: (CameraTattoo) -> Unit
) : ListAdapter<CameraTattoo, CameraTattooAdapter.TattooViewHolder>(TattooDiffCallback()) {

    init {
        setHasStableIds(true) // ✅ important
    }

    override fun getItemId(position: Int): Long {
        // Use a unique & consistent ID for each item
        return getItem(position).id.toLong()
    }

    class TattooViewHolder(private val binding: ItemTattooSquareBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(cameraTattoo: CameraTattoo, onClick: (CameraTattoo) -> Unit) {
            Glide.with(binding.root)
                .load(cameraTattoo.imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(binding.ivTattoo)

            binding.root.setOnClickListener { onClick(cameraTattoo) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TattooViewHolder {
        val binding = ItemTattooSquareBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TattooViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TattooViewHolder, position: Int) {
        holder.bind(getItem(position), onClick)
    }

    class TattooDiffCallback : DiffUtil.ItemCallback<CameraTattoo>() {
        override fun areItemsTheSame(oldItem: CameraTattoo, newItem: CameraTattoo): Boolean {
            // ID must be unique across the list
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CameraTattoo, newItem: CameraTattoo): Boolean {
            return oldItem == newItem
        }
    }
}
