package com.rohans.sacrenachat.model

import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message

class CustomChannel(
    var channel: Channel,
    var lastMessage: Message?
)