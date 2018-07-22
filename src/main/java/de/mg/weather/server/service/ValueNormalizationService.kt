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

        val temperatureRange = temperatureRange(valuesPerTime)

        val pressureMinMax = minMaxSensorValueService.minMax(PRESSURE)
        val humidityMinMax = minMaxSensorValueService.minMax(HUMIDITY)

        fun normalizeValue(type: SensorEnum, value: Float): Float =
                when (type) {
                    TEMPERATURE_1 -> value
                    TEMPERATURE_2 -> value
                    PRESSURE -> normalizeValue(value, pressureMinMax, temperatureRange)
                    HUMIDITY -> normalizeValue(value, humidityMinMax, temperatureRange)
                }

        return valuesPerTime.entries.map { timedEntry ->
            timedEntry.key to
                    timedEntry.value.mapValues { sensorEntry ->
                        normalizeValue(sensorEntry.key, sensorEntry.value)
                    }
        }.toMap()
    }


    // visible for testing
    fun normalizeValue(value: Float, valueRange: MinMax?, targetRange: Range?): Float =
            if (valueRange == null || targetRange == null)
                value
            else if (valueRange.dist() == 0f || targetRange.dist() == 0f)
                targetRange.min
            else
                (value - valueRange.min) / valueRange.dist() * targetRange.dist() + targetRange.min


    private fun temperatureRange(valuesPerTime: Map<LocalDateTime, Map<SensorEnum, Float>>): Range? {

        val relevantValues = valuesPerTime.values.flatMap { sensorValues ->
            listOf(TEMPERATURE_1, TEMPERATURE_2).map { sensorValues[it] }
        }.filterNotNull()

        val min = relevantValues.minBy { it }
        val max = relevantValues.maxBy { it }
        return if (min != null && max != null) Range(min, max) else null
    }

    class Range(val min: Float, val max: Float) {

        fun dist() = max - min
    }
}