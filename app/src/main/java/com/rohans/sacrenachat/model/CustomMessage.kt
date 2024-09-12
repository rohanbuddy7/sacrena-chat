package com.rohans.sacrenachat.model

import io.getstream.chat.android.models.Message

class CustomMessage (
    var hasNext: Boolean,
    var messages: List<Message>
)