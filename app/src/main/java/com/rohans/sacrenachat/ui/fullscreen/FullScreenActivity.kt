package com.rohans.sacrenachat.ui.fullscreen

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.rohans.sacrenachat.R

class FullScreenActivity : AppCompatActivity() {

    private var imagePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full)
        imagePath = intent?.getStringExtra("imagePath");
        Glide.with(this).load(imagePath).into(findViewById(R.id.iv_full))

        val back = findViewById<ImageView>(R.id.iv_back)
        back.setOnClickListener {
            finish()
        }
    }

}