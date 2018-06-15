package de.mg.weather.server

import de.mg.weather.server.service.DBToMemoryReader
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.EnableScheduling


@SpringBootApplication
@EnableScheduling
open class WeatherApp {

    @Autowired
    private lateinit var dbToMemoryReader: DBToMemoryReader

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(WeatherApp::class.java, *args)
        }
    }

    @EventListener(ApplicationReadyEvent::class)
    fun afterStartup() {
        dbToMemoryReader.initialize()
    }

}