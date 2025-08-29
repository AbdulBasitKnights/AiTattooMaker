package com.aspire.social.ai.art.generator.iap.screens.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aspire.social.ai.art.generator.R
import com.aspire.social.ai.art.generator.extension.loadWithGlide
import com.aspire.social.ai.art.generator.iap.model.RewardItem

class RewardAdapter(private val items: List<RewardItem>) :
    RecyclerView.Adapter<RewardAdapter.RewardViewHolder>() {

    class RewardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgIcon: ImageView = itemView.findViewById(R.id.rewardIcon)
        val tvTitle: TextView = itemView.findViewById(R.id.rewardTitle)
        val tvReward: TextView = itemView.findViewById(R.id.reward)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RewardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pro_reward, parent, false)
        return RewardViewHolder(view)
    }

    override fun onBindViewHolder(holder: RewardViewHolder, position: Int) {
        val item = items[position]
//        holder.imgIcon.loadWithGlide(item.iconRes)
//        holder.imgIcon.setImageResource(item.iconRes)
        holder.tvTitle.text = item.title
//        holder.tvReward.text = item.reward
    }

    override fun getItemCount(): Int = items.size
}
