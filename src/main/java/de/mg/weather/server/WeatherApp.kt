package de.mg.weather.server

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.scheduling.annotation.EnableScheduling


@SpringBootApplication
@EnableScheduling
open class WeatherApp {

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(WeatherApp::class.java, *args)
        }
    }

}