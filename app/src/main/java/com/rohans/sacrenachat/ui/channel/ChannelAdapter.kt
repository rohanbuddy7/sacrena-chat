package com.loannetwork.app.ui.billingCompany.bottomsheets.state

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.rohans.sacrenachat.R
import com.rohans.sacrenachat.databinding.CardChannelBinding
import com.rohans.sacrenachat.model.CustomChannel


class ChannelAdapter(
    private var context: Context,
    private var list: ArrayList<CustomChannel>,
    private var clicked: (CustomChannel)->Unit
) : RecyclerView.Adapter<ChannelAdapter.ViewHolder>() {

    class ViewHolder(var binding: CardChannelBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<CardChannelBinding>(
            LayoutInflater.from(context), R.layout.card_channel, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]
        holder.binding.data = data

        holder.itemView.setOnClickListener {
            clicked.invoke(data)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun addData(data: List<CustomChannel>) {
        list.addAll(data);
        list.sortByDescending { it.channel.lastMessageAt }
        notifyDataSetChanged()
    }

    fun clear(){
        list.clear();
        notifyDataSetChanged();
    }
}
