package com.rohans.sacrenachat.utils

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Utils {

    companion object{

        @BindingAdapter("imageUrl")
        @JvmStatic
        fun loadImage(view: ImageView, imageUrl: String?) {
            imageUrl?.let {
                Glide.with(view.context)
                    .load(it)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(view)
            }
        }

        @JvmStatic
        @BindingAdapter("timeFromDate")
        fun setTimeFromDate(textView: TextView, date: Date?) {
            date?.let {
                val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
                textView.text = format.format(date)
            }
        }

        @JvmStatic
        @BindingAdapter("dayFromDate")
        fun setDayFromDate(textView: TextView, date: Date?) {
            date?.let {
                val sdf = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault())
                textView.text = sdf.format(date)
            }
        }

    }
}