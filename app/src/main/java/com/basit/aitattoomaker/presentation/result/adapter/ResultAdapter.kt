package com.basit.aitattoomaker.presentation.result.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.basit.aitattoomaker.R
import com.basit.aitattoomaker.databinding.ItemStyleBinding
import com.basit.aitattoomaker.databinding.ItemTattooSquareBinding
import com.basit.aitattoomaker.extension.dp
import com.basit.aitattoomaker.presentation.ai_create.model.StyleItem
import com.basit.aitattoomaker.presentation.result.model.ResultItem
import com.basit.aitattoomaker.presentation.utils.GradientStrokeDrawable
import com.bumptech.glide.Glide

class ResultAdapter(
    private val onClick: (ResultItem) -> Unit
) : ListAdapter<ResultItem, ResultAdapter.StyleViewHolder>(DiffCallback) {

    // store which position is selected
    private var selectedPos = RecyclerView.NO_POSITION

    init {
        // optional: if you pass the list already, you could set selectedPos here
    }

    fun setInitialSelection(list: List<ResultItem>) {
        selectedPos = list.indexOfFirst { it.isSelected }
        if (selectedPos == -1) selectedPos = RecyclerView.NO_POSITION
    }

    object DiffCallback : DiffUtil.ItemCallback<ResultItem>() {
        override fun areItemsTheSame(oldItem: ResultItem, newItem: ResultItem) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ResultItem, newItem: ResultItem) = oldItem == newItem
    }

    inner class StyleViewHolder(private val binding: ItemTattooSquareBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ResultItem) = with(binding) {
            Glide.with(ivTattoo).load(item.imageUrl).into(ivTattoo)
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
        val binding = ItemTattooSquareBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StyleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StyleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}



