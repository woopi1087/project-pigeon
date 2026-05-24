package com.woopi.pigeon.messagelog

import org.springframework.data.jpa.repository.JpaRepository

interface MessageLogRepository : JpaRepository<MessageLogEntity, Long>
