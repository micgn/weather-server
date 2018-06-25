package de.mg.weather.server.conf

import de.mg.weather.server.model.SensorEnum
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
open class WeatherConfig {

    @Value("\${mqtt.broker.url}")
    var mqttBrokerUrl: String = ""


    val topicToSensor = mapOf("t1" to SensorEnum.TEMPERATURE_1,
            "t2" to SensorEnum.TEMPERATURE_2,
            "p" to SensorEnum.PRESSURE,
            "h" to SensorEnum.HUMIDITY)

    val hoursToShow = 48

    val acceptDataPerSensorAtMaxIntervalSeconds = 50L

}
