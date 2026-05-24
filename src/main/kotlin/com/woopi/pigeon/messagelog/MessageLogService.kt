package com.woopi.pigeon.messagelog

import com.woopi.pigeon.channel.ChannelType
import org.springframework.stereotype.Service

@Service
class MessageLogService(
    private val messageLogRepository: MessageLogRepository,
) {

    fun saveSuccess(
        channel: ChannelType,
        recipient: String,
        title: String?,
        messageId: String,
    ): MessageLogEntity {
        val log = MessageLogEntity(
            channel    = channel,
            recipient  = recipient.take(20),
            title      = title,
            status     = MessageLogStatus.SUCCESS,
            messageId  = messageId,
        )
        return messageLogRepository.save(log)
    }

    fun saveFail(
        channel: ChannelType,
        recipient: String,
        title: String?,
        error: String?,
    ): MessageLogEntity {
        val log = MessageLogEntity(
            channel   = channel,
            recipient = recipient.take(20),
            title     = title,
            status    = MessageLogStatus.FAIL,
            error     = error,
        )
        return messageLogRepository.save(log)
    }
}
