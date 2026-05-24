package com.woopi.pigeon.controller

import com.woopi.pigeon.dto.ApiResponse
import com.woopi.pigeon.dto.SendMessageRequest
import com.woopi.pigeon.service.MessageService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/messages")
class MessageController(
    private val messageService: MessageService,
) {

    @PostMapping("/send")
    fun send(@RequestBody @Valid request: SendMessageRequest): ResponseEntity<ApiResponse<Unit>> {
        messageService.send(request)
        return ResponseEntity.ok(ApiResponse.success("메시지 발송 완료"))
    }
}
