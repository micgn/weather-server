package de.mg.weather.server.service

import de.mg.weather.server.conf.WeatherConfig
import de.mg.weather.server.model.SensorEnum
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
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

    private var previousSensorState = emptySet<SensorEnum>()

    @PostConstruct
    fun init() {
        if (config.alertEmailAddress.isEmpty()) log.warn("no alert email receiver configured")
    }


    @Scheduled(initialDelay = 5 * 60 * 1000, fixedRate = 10 * 60 * 1000)
    fun run() {

        if (config.alertEmailAddress.isEmpty()) return

        val currentSensorState = apiMapperService.activeSensors()

        if (currentSensorState != previousSensorState) {
            send(currentSensorState)
            previousSensorState = currentSensorState
        }
    }


    private fun send(currentSensorState: Set<SensorEnum>) {

        var sensorStateStr = ""
        currentSensorState.joinToString(separator = ", ")
        log.warn("active sensors: $sensorStateStr")

        val message = SimpleMailMessage()
        message.setTo(config.alertEmailAddress)
        message.subject = "Weather Sensors Alert"
        message.text = sensorStateStr
        emailSender.send(message)
    }

}