package de.mg.weather.server.mqtt

import de.mg.weather.server.conf.WeatherConfig
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Profile("!test")
@Component
class MqttListener : MqttCallback {

    private val log = LoggerFactory.getLogger(MqttListener::class.java.name)

    @Autowired
    private lateinit var config: WeatherConfig

    private lateinit var client: MqttClient

    @PostConstruct
    fun init() {

        client = MqttClient(config.mqttBrokerUrl, "WeatherServer")
        client.connect()
        client.setCallback(this)
        client.subscribe(config.topics.keys.toTypedArray())
    }


    override fun messageArrived(topic: String?, message: MqttMessage?) {

    }


    override fun connectionLost(cause: Throwable?) {
        log.error("connection to broker lost", cause)
        // TODO might be useful to reconnect
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {
        // nothing to to
    }
}