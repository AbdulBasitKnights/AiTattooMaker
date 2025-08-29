package com.basit.aitattoomaker.presentation.setting.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.basit.aitattoomaker.R
import com.basit.aitattoomaker.extension.hide


private const val VIEW_TYPE_HEADER = 0
private const val VIEW_TYPE_BODY = 1

class SettingsAdapter(
    private val context: Context,
    private var list: List<ModelSettings>,
    private var listener : SettingsClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val rootView =
                LayoutInflater.from(context).inflate(R.layout.item_settings_header, parent, false)
            SettingsHeaderViewHolder(rootView)
        } else {
            val rootView =
                LayoutInflater.from(context).inflate(R.layout.item_settings, parent, false)
            SettingsViewHolder(rootView)
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = list[position]
        if (holder.itemViewType == VIEW_TYPE_HEADER) {
            (holder as SettingsHeaderViewHolder)
            holder.header.text = item.title

        } else {
            try {
                (holder as SettingsViewHolder)
                holder.title.text = item.title
                holder.background.background = item.background
                if (position == list.size - 1) holder.view.hide()
                if (!item.showView) holder.view.hide()
                //   Glide.with(context).load(item.icon).into(holder.icon) //using this lags the recyclerview.
                item?.icon?.let {
                    holder.icon.setImageResource(item.icon)
                }
                holder.itemView.setOnClickListener {
                    listener.onSettingItemClick(item.title)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int {
        return if (list[position].isHeader) VIEW_TYPE_HEADER else VIEW_TYPE_BODY
    }


    @SuppressLint("NotifyDataSetChanged")
    fun updateList(list: List<ModelSettings>) {
        this.list = list
        notifyDataSetChanged()
    }

    inner class SettingsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val icon: ImageView = itemView.findViewById(R.id.icon)
        val background: ConstraintLayout = itemView.findViewById(R.id.rootLayout)
        val view: View = itemView.findViewById(R.id.view)

    }

    inner class SettingsHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val header: TextView = itemView.findViewById(R.id.tv_header)

    }
}

interface SettingsClickListener {
    fun onSettingItemClick(which: String)
}