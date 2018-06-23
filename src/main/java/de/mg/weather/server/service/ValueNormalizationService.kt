package de.mg.weather.server.service

import de.mg.weather.server.model.SensorEnum
import de.mg.weather.server.model.SensorEnum.*
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * the values for pressure and humidity are normalized to the range of the temperatures,
 * so that they can be shown within the same y-axis on the chart in the front end
 */
@Component
class ValueNormalizationService {

    fun normalizeValues(valuesPerTime: Map<LocalDateTime, Map<SensorEnum, Float>>): Map<LocalDateTime, Map<SensorEnum, Float>> {

        if (valuesPerTime.isEmpty()) return emptyMap()

        val temperatureRange = minMax(valuesPerTime, listOf(TEMPERATURE_1, TEMPERATURE_2))
        val pressureRange = minMax(valuesPerTime, listOf(PRESSURE))
        val humidityRange = minMax(valuesPerTime, listOf(HUMIDITY))

        fun normalizeValue(type: SensorEnum, value: Float): Float =
                when (type) {
                    TEMPERATURE_1 -> value
                    TEMPERATURE_2 -> value
                    PRESSURE -> normalizeValue(value, pressureRange, temperatureRange)
                    HUMIDITY -> normalizeValue(value, humidityRange, temperatureRange)
                }

        return valuesPerTime.entries.map { timedEntry ->
            timedEntry.key to
                    timedEntry.value.mapValues { sensorEntry ->
                        normalizeValue(sensorEntry.key, sensorEntry.value)
                    }
        }.toMap()
    }

    private fun minMax(valuesPerTime: Map<LocalDateTime, Map<SensorEnum, Float>>, types: List<SensorEnum>): Range? {

        val relevantValues = valuesPerTime.values.flatMap { sensorValues ->
            types.map { sensorValues[it] }
        }.filterNotNull()

        val min = relevantValues.minBy { it }
        val max = relevantValues.maxBy { it }
        return if (min != null && max != null) Range(min, max) else null
    }


    // visible for testing
    fun normalizeValue(value: Float, valueRange: Range?, targetRange: Range?): Float =
            if (valueRange == null || targetRange == null)
                value
            else if (valueRange.dist() == 0f || targetRange.dist() == 0f)
                targetRange.min
            else
                (value - valueRange.min) / valueRange.dist() * targetRange.dist() + targetRange.min


    class Range(val min: Float, private val max: Float) {
        fun dist() = max - min
    }
}