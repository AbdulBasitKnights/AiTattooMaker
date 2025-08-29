package com.aspire.social.ai.art.generator.iap.screens.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aspire.social.ai.art.generator.R
import com.aspire.social.ai.art.generator.databinding.SliderItemBinding
import com.aspire.social.ai.art.generator.extension.loadWithGlide

class ProSliderAdapter(
    context: Context?
) : RecyclerView.Adapter<ProSliderAdapter.SliderViewHolder>() {

    private val imgs = intArrayOf(
        R.drawable.ads,
        R.drawable.models,
        R.drawable.watermark
    )


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProSliderAdapter.SliderViewHolder {
        val binding = SliderItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SliderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProSliderAdapter.SliderViewHolder, position: Int) {
        val actualPosition = position % imgs.size
        val image = imgs[actualPosition]

        holder.bind(image)
    }

    override fun getItemCount(): Int {
        return Integer.MAX_VALUE

    }

    inner class SliderViewHolder(private val binding: SliderItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(imgRes: Int) {
            binding.imageView.loadWithGlide(imgRes)
        }
    }

}
