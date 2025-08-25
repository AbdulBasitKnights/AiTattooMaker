package com.basit.aitattoomaker.presentation.ai_create.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.basit.aitattoomaker.R
import com.basit.aitattoomaker.databinding.ItemStyleBinding
import com.basit.aitattoomaker.extension.dp
import com.basit.aitattoomaker.presentation.ai_create.model.StyleItem
import com.basit.aitattoomaker.presentation.utils.GradientStrokeDrawable
import com.bumptech.glide.Glide

class StyleAdapter(
    private val onClick: (StyleItem) -> Unit
) : ListAdapter<StyleItem, StyleAdapter.StyleViewHolder>(DiffCallback) {

    // store which position is selected
    private var selectedPos = RecyclerView.NO_POSITION

    init {
        // optional: if you pass the list already, you could set selectedPos here
    }

    fun setInitialSelection(list: List<StyleItem>) {
        selectedPos = list.indexOfFirst { it.isSelected }
        if (selectedPos == -1) selectedPos = RecyclerView.NO_POSITION
    }

    object DiffCallback : DiffUtil.ItemCallback<StyleItem>() {
        override fun areItemsTheSame(oldItem: StyleItem, newItem: StyleItem) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: StyleItem, newItem: StyleItem) = oldItem == newItem
    }

    inner class StyleViewHolder(private val binding: ItemStyleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: StyleItem) = with(binding) {
            tvTattooName.text = item.title
            Glide.with(ivTattoo).load(item.url).into(ivTattoo)

            // highlight with gradient stroke if selected
            if (item.isSelected) {
                root.background = GradientStrokeDrawable(
                    radiusPx = root.dp(10),
                    strokePx = root.dp(2),
                    startColor = ContextCompat.getColor(root.context, R.color.colorprimary),
                    endColor = ContextCompat.getColor(root.context, R.color.colorsecondary),
                    angleDeg = 0f,
                    fillColor  = Color.TRANSPARENT
                )
            } else {
                root.background = ContextCompat.getDrawable(root.context, R.drawable.bg_style_item_normal)
            }

            root.setOnClickListener {
                val old = selectedPos
                val new = adapterPosition
                if (new == RecyclerView.NO_POSITION) return@setOnClickListener

                if (old != new) {
                    selectedPos = new
                    if (old != RecyclerView.NO_POSITION) notifyItemChanged(old)
                    notifyItemChanged(new)
                }

                onClick(item)
            }
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



