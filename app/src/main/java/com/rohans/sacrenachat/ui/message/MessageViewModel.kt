package com.rohans.sacrenachat.ui.message

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rohans.sacrenachat.model.CustomMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.ChatEventListener
import io.getstream.chat.android.client.api.models.Pagination
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Message
import io.getstream.result.Error
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MessageViewModel @Inject constructor(
    var chatClient: ChatClient
): ViewModel() {

    private var _message = MutableLiveData<CustomMessage>();
    var message: LiveData<CustomMessage> = _message;
    private var _newMessage = MutableLiveData<Message>();
    var newMessage: LiveData<Message> = _newMessage;

    fun getMessage(channelId: String, pageSize: Int){
        val channelClient = chatClient.channel("messaging", channelId)

        val requestX = QueryChannelRequest().withMessages(pageSize);
        getMessageQuery(channelClient, requestX, pageSize)
    }

    fun getNexMessage(channelId: String, message: Message, pageSize: Int){
        val channelClient = chatClient.channel("messaging", channelId)
        val nextRequest = QueryChannelRequest()
            .withMessages(Pagination.LESS_THAN, message.id, pageSize)
        getMessageQuery(channelClient, nextRequest, pageSize);
    }


    private fun getMessageQuery(channelClient: ChannelClient, request: QueryChannelRequest, pageSize: Int){
        channelClient.query(request).enqueue { result ->
            if (result.isSuccess) {
                result.map {
                    val messages: List<Message> = it.messages.sortedByDescending { it.updatedAt }
                    val hasNext: Boolean = messages.size >= pageSize
                    _message.postValue(CustomMessage(hasNext, messages));
                }
            }
        }
    }


    fun sendMessage(text: String, channelId: String){
        val channelClient = chatClient.channel("messaging", channelId)
        val message = Message(
            text = text
        )

        channelClient.sendMessage(message).enqueue { result ->
            if (result.isSuccess) {
                result.onSuccess {

                }
            } else {
                Log.e("TAG", "onViewCreated error", )
            }
        }
    }

    fun getNewMessage(channelId: String){
        val channelClient = chatClient.channel("messaging", channelId)
        channelClient.subscribe(listener = ChatEventListener {
            Log.e("TAG", "getNewMessage: $it")
            when(it.type){
                "message.new"->{
                    val new = it as NewMessageEvent;
                    _newMessage.postValue(new.message);
                }
                "notification.message_new"->{
                    val new = it as NotificationMessageNewEvent;
                    _newMessage.postValue(new.message);
                }
            }
        })
    }

    fun sendFile(channelId: String, imageFile: File){
        Log.e("TAG", "sendFilexxd: ${imageFile} --> ${imageFile.length()}");
        val channelClient = chatClient.channel("messaging", channelId)
        channelClient.sendImage(
            imageFile, object : ProgressCallback {
                override fun onSuccess(url: String?) {
                    val fileUrl = url
                }

                override fun onError(error: Error) {}

                override fun onProgress(bytesUploaded: Long, totalBytes: Long) {}
            }
        ).enqueue { result ->
            if (result.isSuccess) {
                result.map {
                    val attachment = Attachment(
                        type = "image",
                        imageUrl = it.file,
                        mimeType = "image/jpeg",
                    )
                    val message = Message(
                        attachments = mutableListOf(attachment),
                    )
                    channelClient.sendMessage(message).enqueue {}
                }
            }
        }
    }

}