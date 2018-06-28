package de.mg.weather.server.conf

import de.mg.weather.server.model.SensorEnum
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import java.time.LocalDateTime

@Configuration
open class WeatherConfig {

    @Value("\${mqtt.broker.url}")
    var mqttBrokerUrl: String = ""


    val topicToSensor = mapOf("t1" to SensorEnum.TEMPERATURE_1,
            "t2" to SensorEnum.TEMPERATURE_2,
            "p" to SensorEnum.PRESSURE,
            "h" to SensorEnum.HUMIDITY)

    val hoursToShow = 48

    fun showSince() = LocalDateTime.now().minusHours(hoursToShow.toLong())


    val acceptDataPerSensorAtMaxIntervalSeconds = 50L

}
