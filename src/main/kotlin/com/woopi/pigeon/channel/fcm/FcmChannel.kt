package com.woopi.pigeon.channel.fcm

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import com.woopi.pigeon.channel.ChannelType
import com.woopi.pigeon.channel.MessageChannel
import com.woopi.pigeon.dto.SendMessageRequest
import com.woopi.pigeon.messagelog.MessageLogService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class FcmChannel(
    private val messageLogService: MessageLogService,
) : MessageChannel {

    private val log = LoggerFactory.getLogger(FcmChannel::class.java)

    override val channelType = ChannelType.FCM

    override fun send(request: SendMessageRequest) {
        val tokenPreview = request.to.take(20)

        log.info(
            "[FCM] 발송 시작 | token={}, title={}, data={}",
            tokenPreview,
            request.title,
            request.data,
        )

        val message = Message.builder()
            .setToken(request.to)
            .apply {
                if (request.title != null || request.body != null) {
                    setNotification(
                        Notification.builder()
                            .setTitle(request.title)
                            .setBody(request.body)
                            .build()
                    )
                }
                if (request.data.isNotEmpty()) {
                    putAllData(request.data)
                }
            }
            .build()

        try {
            val messageId = FirebaseMessaging.getInstance().send(message)
            log.info("[FCM] 발송 성공 | token={}, messageId={}", tokenPreview, messageId)
            messageLogService.saveSuccess(
                channel   = ChannelType.FCM,
                recipient = request.to,
                title     = request.title,
                messageId = messageId,
            )
        } catch (e: Exception) {
            log.error("[FCM] 발송 실패 | token={}, error={}", tokenPreview, e.message, e)
            messageLogService.saveFail(
                channel   = ChannelType.FCM,
                recipient = request.to,
                title     = request.title,
                error     = e.message,
            )
            throw e
        }
    }
}
