package de.mg.weather.server.model

import de.mg.weather.server.model.SensorEnum.*
import org.springframework.stereotype.Component

@Component
open class SensorData {

    val sensorTimeDistanceMinutes = 2L

    val sensorsMap = mapOf(
            TEMPERATURE_1 to SensorTypeData(TEMPERATURE_1),
            TEMPERATURE_2 to SensorTypeData(TEMPERATURE_2),
            HUMIDITY to SensorTypeData(HUMIDITY),
            PRESSURE to SensorTypeData(PRESSURE)
    )

}