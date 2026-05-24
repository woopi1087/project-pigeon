# channel 패키지 — 메시지 채널 전략 패턴

## 구조

```
channel/
├── ChannelType.kt       # 지원 채널 enum
├── MessageChannel.kt    # 채널 인터페이스 (전략)
└── fcm/
    └── FcmChannel.kt    # FCM 구현체
```

## 동작 방식

`MessageService`가 Spring으로부터 `List<MessageChannel>`을 주입받아,  
요청의 `channel` 필드와 일치하는 구현체를 찾아 `send()`를 위임합니다.

```
POST /api/messages/send { channel: "FCM", ... }
       ↓
MessageService.send()
       ↓
channels.find { it.channelType == FCM }
       ↓
FcmChannel.send()  →  Firebase API  →  MessageLogService.saveSuccess/saveFail()
```

## 채널 구현 현황

| 채널 | 클래스 | 상태 |
|------|--------|------|
| FCM | `FcmChannel` | ✅ 구현 완료 |
| SMS | — | 미구현 |
| EMAIL | — | 미구현 |
| KAKAO | — | 미구현 |

## 새 채널 추가 방법

1. `ChannelType`에 enum 값 추가
2. `MessageChannel` 구현 후 `@Component` 등록

```kotlin
@Component
class SmsChannel(
    private val messageLogService: MessageLogService,
) : MessageChannel {
    override val channelType = ChannelType.SMS

    override fun send(request: SendMessageRequest) {
        // SMS 발송 로직
        // 성공/실패 시 messageLogService.saveSuccess / saveFail 호출
    }
}
```

3. 끝 — `MessageService`가 자동으로 인식합니다.
