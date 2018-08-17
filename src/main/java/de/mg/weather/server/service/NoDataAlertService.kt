package de.mg.weather.server.service

import de.mg.weather.server.conf.WeatherConfig
import de.mg.weather.server.model.SensorEnum
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime.now
import javax.annotation.PostConstruct


@Component
class NoDataAlertService {

    private val log = LoggerFactory.getLogger(NoDataAlertService::class.java.name)


    @Autowired
    lateinit var config: WeatherConfig

    @Autowired(required = false)
    lateinit var emailSender: JavaMailSender

    @Autowired
    lateinit var apiMapperService: ApiMapperService

    private var previousSensorState = mapOf<SensorEnum, Boolean>()

    @PostConstruct
    fun init() {
        if (config.alertEmailAddress.isEmpty()) log.warn("no alert email receiver configured")
    }


    @Scheduled(initialDelay = 15 * 60 * 1000, fixedRate = 15 * 60 * 1000 /*every 10 minutes*/)
    fun run() {

        if (config.alertEmailAddress.isEmpty()) return

        val lastAcceptableTime = Utils.epoch(now().minusMinutes(config.downtimeBeforeAltertMinutes))

        val currentSensorState = apiMapperService.currentValuesMap().mapValues {
            it.value != null && it.value!!.time >= lastAcceptableTime
        }

        if (currentSensorState != previousSensorState)
            send(currentSensorState)

        previousSensorState = currentSensorState
    }


    private fun send(currentSensorState: Map<SensorEnum, Boolean>) {

        var sensorStateStr = ""
        currentSensorState.forEach { sensor, ok -> sensorStateStr += "$sensor : $ok\n" }
        log.warn("new sensor state: $sensorStateStr")

        val message = SimpleMailMessage()
        message.setTo(config.alertEmailAddress)
        message.subject = "Weather Sensors Alert"
        message.text = sensorStateStr
        emailSender.send(message)
    }

}