package de.mg.weather.server.conf

import de.mg.weather.server.db.SensorEnum
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
open class WeatherConfig {

    @Value("\${mqtt.broker.url}")
    var mqttBrokerUrl: String = ""


    val topics = mapOf("t1" to SensorEnum.TEMPERATURE_1,
            "t2" to SensorEnum.TEMPERATURE_2,
            "p" to SensorEnum.PRESSURE,
            "h" to SensorEnum.HUMIDITY)

}
