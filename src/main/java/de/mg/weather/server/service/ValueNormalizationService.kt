package de.mg.weather.server.service

import de.mg.weather.server.model.SensorEnum
import de.mg.weather.server.model.SensorEnum.*
import de.mg.weather.server.service.MinMaxSensorValueService.MinMax
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * the values for pressure and humidity are normalized to the range of the temperatures,
 * so that they can be shown within the same y-axis on the chart in the front end
 */
@Component
open class ValueNormalizationService {

    @Autowired
    lateinit var minMaxSensorValueService: MinMaxSensorValueService


    fun normalizeValues(valuesPerTime: Map<LocalDateTime, Map<SensorEnum, Float>>): Map<LocalDateTime, Map<SensorEnum, Float>> {

        if (valuesPerTime.isEmpty()) return emptyMap()

        val t1MinMax = minMaxSensorValueService.minMax(TEMPERATURE_1)
        val t2MinMax = minMaxSensorValueService.minMax(TEMPERATURE_2)
        val temperatureMinMax = t1MinMax?.getMerged(t2MinMax) ?: t2MinMax?.getMerged(t1MinMax)

        val pressureMinMax = minMaxSensorValueService.minMax(PRESSURE)
        val humidityMinMax = minMaxSensorValueService.minMax(HUMIDITY)

        fun normalizeValue(type: SensorEnum, value: Float): Float =
                when (type) {
                    TEMPERATURE_1 -> value
                    TEMPERATURE_2 -> value
                    PRESSURE -> normalizeValue(value, pressureMinMax, temperatureMinMax)
                    HUMIDITY -> normalizeValue(value, humidityMinMax, temperatureMinMax)
                }

        return valuesPerTime.entries.map { timedEntry ->
            timedEntry.key to
                    timedEntry.value.mapValues { sensorEntry ->
                        normalizeValue(sensorEntry.key, sensorEntry.value)
                    }
        }.toMap()
    }


    // visible for testing
    fun normalizeValue(value: Float, valueRange: MinMax?, targetRange: MinMax?): Float =
            if (valueRange == null || targetRange == null)
                value
            else if (valueRange.dist() == 0f || targetRange.dist() == 0f)
                targetRange.min
            else
                (value - valueRange.min) / valueRange.dist() * targetRange.dist() + targetRange.min

}