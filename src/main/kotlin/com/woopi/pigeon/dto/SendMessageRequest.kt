package com.woopi.pigeon.dto

import com.woopi.pigeon.channel.ChannelType
import jakarta.validation.constraints.NotBlank

data class SendMessageRequest(
    val channel: ChannelType,
    @field:NotBlank val to: String,
    val title: String? = null,
    val body: String? = null,
    val data: Map<String, String> = emptyMap(),
)
