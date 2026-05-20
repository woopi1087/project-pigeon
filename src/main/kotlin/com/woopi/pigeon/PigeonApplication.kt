package com.woopi.pigeon

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PigeonApplication

fun main(args: Array<String>) {
	runApplication<PigeonApplication>(*args)
}
