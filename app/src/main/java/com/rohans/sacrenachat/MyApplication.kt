package com.rohans.sacrenachat

import android.app.Application
import com.rohans.sacrenachat.di.ChatModule
import dagger.hilt.android.HiltAndroidApp
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.User
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application()