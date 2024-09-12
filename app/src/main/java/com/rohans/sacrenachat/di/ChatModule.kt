package com.rohans.sacrenachat.di

import android.content.Context
import com.rohans.sacrenachat.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ChatModule {

    @Provides
    @Singleton
    fun provideClient(
        @ApplicationContext context: Context
    ): ChatClient{
        val apiKey = "9htvw47z37sd" //BuildConfig.STREAM_IO_API_KEY
        return ChatClient.Builder(apiKey = apiKey, context)
            .logLevel(ChatLogLevel.ALL)
            .build()
    }

}