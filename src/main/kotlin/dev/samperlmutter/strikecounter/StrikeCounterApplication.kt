package dev.samperlmutter.strikecounter

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class StrikeCounterApplication

fun main(args: Array<String>) {
	runApplication<StrikeCounterApplication>(*args)
}
