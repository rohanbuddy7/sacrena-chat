package com.rohans.sacrenachat.ui.channel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rohans.sacrenachat.model.CustomChannel
import com.rohans.sacrenachat.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.models.querysort.QuerySortByField.Companion.descByName
import javax.inject.Inject

@HiltViewModel
class ChannelViewModel @Inject constructor(
    private var chatClient: ChatClient
): ViewModel() {

    private var aliceUserId = Constants.aliceUserId
    private var _channels = MutableLiveData<List<CustomChannel>>();
    var channels: LiveData<List<CustomChannel>> = _channels;

    fun clearChannel(){
        _channels.value = emptyList();
    }

    fun connectUser(){
        val user = User(
            id = aliceUserId,
            name = "alice",
            image = "https://bit.ly/2TIt8NR",
        )

        chatClient.connectUser(
            user = user,
            token = chatClient.devToken(aliceUserId),
        ).enqueue { result ->
            if (result.isSuccess) {
                result.onSuccess {
                    it.user
                }
            } else {
                Log.e("error", "connect user failed")
            }
        }
    }


    fun queryChannel(){
        val filter = Filters.and(
            Filters.eq("type", "messaging"),
            Filters.`in`("members", listOf(aliceUserId)),
        )
        val sort = QuerySortByField<Channel>().descByName("last_message_at")

        val request = QueryChannelsRequest(
            filter = filter,
            offset = 0,
            limit = 10,
            querySort = sort
        ).withWatch().withState()

        chatClient.queryChannels(request).enqueue { result ->
            if (result.isSuccess) {
                result.onSuccess {listChannel->
                    var size = listChannel.size;
                    val list = arrayListOf<CustomChannel>()
                    listChannel.map {channel->

                        //getting last message
                        val channelClient = chatClient.channel("messaging", channel.id)
                        val pageSize = 1
                        val requestX = QueryChannelRequest().withMessages(pageSize)
                        var lastMessage: Message? = null;
                        channelClient.query(requestX).enqueue { result ->
                            if (result.isSuccess) {
                                 result.map {
                                     lastMessage = it.messages.firstOrNull();
                                     list.add(CustomChannel(it, lastMessage))
                                     if(size == list.size){
                                         _channels.postValue(list)
                                     }
                                }
                            }
                        }
                    }
                }
            } else {
                Log.e("error", "query channel")
            }
        }
    }


}