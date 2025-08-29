package com.basit.aitattoomaker.presentation.iap.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.basit.aitattoomaker.R
import com.bumptech.glide.Glide

class ImageAdapter(private val images: ArrayList<Int>,private val context: Context) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.slide_item, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        try {
            Glide.with(context)
                .load(images[position % images.size])
                .into(holder.imageView)
        }
        catch (e:Exception){
            //
        }

    }

    override fun getItemCount(): Int {
        return Int.MAX_VALUE
    }
}