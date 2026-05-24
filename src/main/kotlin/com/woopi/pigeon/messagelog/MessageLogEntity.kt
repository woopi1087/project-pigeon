package com.woopi.pigeon.messagelog

import com.woopi.pigeon.channel.ChannelType
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "message_logs",
    indexes = [
        Index(name = "idx_message_logs_created_at", columnList = "created_at DESC"),
        Index(name = "idx_message_logs_status",     columnList = "status"),
        Index(name = "idx_message_logs_channel",    columnList = "channel"),
    ]
)
class MessageLogEntity(

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val channel: ChannelType,

    @Column(nullable = false, length = 512)
    val recipient: String,           // 수신자 식별자 (FCM 토큰 앞 20자 등 마스킹 처리)

    @Column(length = 255)
    val title: String?,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    var status: MessageLogStatus,

    @Column(length = 255)
    var messageId: String? = null,   // 채널 발급 ID (FCM messageId 등)

    @Column(columnDefinition = "TEXT")
    var error: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
}
