package com.woopi.pigeon.channel.fcm

class UnregisteredTokenException(val token: String) : RuntimeException("FCM 토큰이 만료되었습니다: ${token.take(20)}")
