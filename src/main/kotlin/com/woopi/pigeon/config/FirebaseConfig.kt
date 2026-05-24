package com.woopi.pigeon.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource

@Configuration
class FirebaseConfig(
    @Value("\${firebase.credentials-path}") private val credentialsPath: String,
) {

    @Bean
    fun firebaseApp(): FirebaseApp {
        if (FirebaseApp.getApps().isNotEmpty()) return FirebaseApp.getInstance()

        val credentials = try {
            GoogleCredentials.getApplicationDefault()
        } catch (e: Exception) {
            // GOOGLE_APPLICATION_CREDENTIALS 미설정 시 classpath 파일 사용
            GoogleCredentials.fromStream(ClassPathResource(credentialsPath).inputStream)
        }

        val options = FirebaseOptions.builder()
            .setCredentials(credentials)
            .build()

        return FirebaseApp.initializeApp(options)
    }
}
