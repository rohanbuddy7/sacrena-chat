package com.loannetwork.app.ui.billingCompany.bottomsheets.state

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rohans.sacrenachat.R
import com.rohans.sacrenachat.databinding.CardMessageMineBinding
import com.rohans.sacrenachat.databinding.CardMessageOtherBinding
import com.rohans.sacrenachat.utils.Constants
import io.getstream.chat.android.models.Message
import java.util.Date


class MessageAdapter(
    private var context: Context,
    private var list: ArrayList<Message>,
    private var imageClicked: (Message)->Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var previousDate: Date? = null;
    private var aliceId = Constants.aliceUserId

    companion object {
        private const val VIEW_TYPE_MINE = 1
        private const val VIEW_TYPE_OTHERS = 2
    }

    class ViewHolderMine(var binding: CardMessageMineBinding) : RecyclerView.ViewHolder(binding.root)
    class ViewHolderOther(var binding: CardMessageOtherBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        return if (list.get(position).user.id == aliceId) {
            VIEW_TYPE_MINE;
        } else {
            VIEW_TYPE_OTHERS;
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when(viewType){
            VIEW_TYPE_MINE->{
                val binding = DataBindingUtil.inflate<CardMessageMineBinding>(LayoutInflater.from(context), R.layout.card_message_mine, parent, false)
                return ViewHolderMine(binding)
            }
            VIEW_TYPE_OTHERS->{
                val binding = DataBindingUtil.inflate<CardMessageOtherBinding>(LayoutInflater.from(context), R.layout.card_message_other, parent, false)
                return ViewHolderOther(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        //holder.setIsRecyclable(false)
        var showdate = false;
        if(previousDate?.date != list[position].updatedAt?.date){
            previousDate = list[position].updatedAt;
            showdate = true;
        }

        when(holder){
            is ViewHolderMine->{
                val data = list[position]
                holder.binding.data = data
                if(showdate){
                    holder.binding.layoutDateMine.cardView.visibility = View.VISIBLE
                } else {
                    holder.binding.layoutDateMine.cardView.visibility = View.GONE
                }

                holder.binding.ivImage.setOnClickListener {
                    imageClicked.invoke(data)
                }
            }
            is ViewHolderOther->{
                val data = list[position]
                holder.binding.data = data
                Glide.with(context).load(data.user.image).into(holder.binding.ivProfile)
                if(showdate){
                    holder.binding.layoutDateOther.cardView.visibility = View.VISIBLE
                } else {
                    holder.binding.layoutDateOther.cardView.visibility = View.GONE
                }

                holder.binding.ivImage.setOnClickListener {
                    imageClicked.invoke(data)
                }
            }
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun addData(data: List<Message>) {
        val prev = list.size
        list.addAll(data);
        notifyItemRangeInserted(prev, list.size);
        //notifyDataSetChanged();
    }

    fun addNewData(data: Message) {
        list.add(0, data);
        notifyItemInserted(0)
    }
}
