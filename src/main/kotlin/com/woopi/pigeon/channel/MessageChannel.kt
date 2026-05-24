package com.woopi.pigeon.channel

import com.woopi.pigeon.dto.SendMessageRequest

interface MessageChannel {
    val channelType: ChannelType
    fun send(request: SendMessageRequest)
}
