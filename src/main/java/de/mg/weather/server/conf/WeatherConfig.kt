package de.mg.weather.server.conf

import de.mg.weather.server.model.SensorEnum
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import java.time.LocalDateTime
import java.util.*
import javax.annotation.PostConstruct

@Configuration
open class WeatherConfig {

    private val log = LoggerFactory.getLogger(WeatherConfig::class.java.name)


    @Value("\${mqtt.broker.url}")
    var mqttBrokerUrl: String = ""

    @Value("\${mqtt.broker.user:}")
    var mqttBrokerUser: String = ""

    @Value("\${mqtt.broker.password:}")
    var mqttBrokerPassword: String = ""

    fun isBrokerAuth() = mqttBrokerUser.isNotBlank() && mqttBrokerPassword.isNotBlank()


    val topicToSensor = mapOf("t1" to SensorEnum.TEMPERATURE_1,
            "t2" to SensorEnum.TEMPERATURE_2,
            "p" to SensorEnum.PRESSURE,
            "h" to SensorEnum.HUMIDITY)

    val hoursToShow = 48

    fun showSince() = LocalDateTime.now().minusHours(hoursToShow.toLong())!!


    val acceptDataPerSensorAtMaxIntervalSeconds = 50L


    @Value("\${basicAuth.user:}")
    var basicAuthUser: String? = null

    @Value("\${basicAuth.password:}")
    var basicAuthPassword: String? = null

    fun basicAuthEncoded(): String? {
        if (basicAuthUser.isNullOrBlank() || basicAuthPassword.isNullOrBlank()) return null

        val userPw = "$basicAuthUser:$basicAuthPassword"
        val encodedBytes = Base64.getEncoder().encode(userPw.toByteArray())
        return String(encodedBytes)
    }

    @PostConstruct
    fun init() {
        log.info(toString())
    }

    override fun toString(): String {
        return "WeatherConfig(mqttBrokerUrl='$mqttBrokerUrl', mqttBrokerUser='$mqttBrokerUser', mqttBrokerPassword='$mqttBrokerPassword', hoursToShow=$hoursToShow, acceptDataPerSensorAtMaxIntervalSeconds=$acceptDataPerSensorAtMaxIntervalSeconds, basicAuthUser=$basicAuthUser, basicAuthPassword=$basicAuthPassword)"
    }


}
