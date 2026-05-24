package com.woopi.pigeon.service

import com.woopi.pigeon.channel.MessageChannel
import com.woopi.pigeon.dto.SendMessageRequest
import org.springframework.stereotype.Service

@Service
class MessageService(
    private val channels: List<MessageChannel>,
) {

    fun send(request: SendMessageRequest) {
        val channel = channels.find { it.channelType == request.channel }
            ?: throw UnsupportedOperationException("지원하지 않는 채널: ${request.channel}")
        channel.send(request)
    }
}
