package de.codinghaus.hellorating

import org.springframework.boot.Banner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HelloRatingApplication

fun main(args: Array<String>) {
	runApplication<HelloRatingApplication>(*args) {
		setBannerMode(Banner.Mode.OFF)
	}
}
