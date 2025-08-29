package com.basit.aitattoomaker.presentation.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.basit.aitattoomaker.databinding.HistoryListItemBinding
import com.basit.aitattoomaker.presentation.history.model.Creation
import com.bumptech.glide.Glide

class CreationAdapter(
    private val onClick: (Creation) -> Unit
) : ListAdapter<Creation, CreationAdapter.CreationViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<Creation>() {
        override fun areItemsTheSame(oldItem: Creation, newItem: Creation): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Creation, newItem: Creation): Boolean =
            oldItem == newItem
    }

    inner class CreationViewHolder(
        private val binding: HistoryListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Creation) {
            // Load image with Glide (works with assets, URL, drawable)
            Glide.with(binding.root)
                .load(item.imageUrl)
                .into(binding.ivImg)

            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreationViewHolder {
        val binding = HistoryListItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CreationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CreationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
