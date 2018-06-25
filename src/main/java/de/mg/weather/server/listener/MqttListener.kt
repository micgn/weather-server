package de.mg.weather.server.listener

import de.mg.weather.server.conf.WeatherConfig
import de.mg.weather.server.model.SensorDataContainer
import de.mg.weather.server.model.SensorDataEntry
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.LocalDateTime.now
import javax.annotation.PostConstruct

@Profile("!test")
@Component
class MqttListener : MqttCallback {

    private val log = LoggerFactory.getLogger(MqttListener::class.java.name)

    @Autowired
    private lateinit var config: WeatherConfig

    @Autowired
    private lateinit var sensorDataContainer: SensorDataContainer


    private lateinit var client: MqttClient

    @PostConstruct
    fun init() {

        client = MqttClient(config.mqttBrokerUrl, "WeatherServer")
        client.connect()
        client.setCallback(this)
        client.subscribe(config.topicToSensor.keys.toTypedArray())
    }


    override fun messageArrived(topic: String?, message: MqttMessage?) {

        if (topic == null || message == null)
            return

        val type = config.topicToSensor[topic]
        if (type == null) {
            log.error("received unexpected topic: '$topic'")
            return
        }

        val timestamp = now()
        val messageStr = String(message.payload)
        val value =
                try {
                    messageStr.toFloat()
                } catch (e: NumberFormatException) {
                    log.error("received invalid payload: '$messageStr'")
                    return
                }

        log.debug("topic: '$topic', message: '$messageStr'")

        val last = sensorDataContainer.sensorsMap[type]!!.lastReceived

        // at most every 50s a message may be received per sensor type
        val lastValue = last.get()
        if (lastValue == null ||
                lastValue.time.isBefore(now().minusSeconds(config.acceptDataPerSensorAtMaxIntervalSeconds)))
            last.set(SensorDataEntry(timestamp, value))
        else
            log.warn("too many messages for $type: at $timestamp, before at ${lastValue.time}")
        return
    }


    override fun connectionLost(cause: Throwable?) {
        log.error("connection to broker lost", cause)
        // TODO might be useful to reconnect
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {
        // nothing to to
    }
}