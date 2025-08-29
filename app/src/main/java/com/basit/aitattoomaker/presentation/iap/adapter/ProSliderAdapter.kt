package com.basit.aitattoomaker.presentation.iap.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.basit.aitattoomaker.R
import com.basit.aitattoomaker.databinding.SliderItemBinding

class ProSliderAdapter(
    context: Context?
) : RecyclerView.Adapter<ProSliderAdapter.SliderViewHolder>() {
//
//    private val imgs = intArrayOf(
//        R.drawable.ads,
//        R.drawable.models,
//        R.drawable.watermark
//    )


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SliderViewHolder {
        val binding = SliderItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SliderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
//        val actualPosition = position % imgs.size
//        val image = imgs[actualPosition]

//        holder.bind(image)
    }

    override fun getItemCount(): Int {
        return Integer.MAX_VALUE

    }

    inner class SliderViewHolder(private val binding: SliderItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(imgRes: Int) {
//            binding.imageView.loadWithGlide(imgRes)
        }
    }

}
