package de.mg.weather.server.service

import de.mg.weather.server.api.PeriodMinMax
import de.mg.weather.server.api.SensorTimeValue
import de.mg.weather.server.conf.WeatherConfig
import de.mg.weather.server.model.SensorDataContainer
import de.mg.weather.server.model.SensorEnum
import de.mg.weather.server.model.SensorEnum.PRESSURE
import de.mg.weather.server.model.SensorEnum.values
import de.mg.weather.server.service.Utils.epoch
import de.mg.weather.server.service.Utils.pa2hPa
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.LocalDateTime.now

@Component
class ApiMapperService {

    @Autowired
    lateinit var sensorDataContainer: SensorDataContainer

    @Autowired
    lateinit var valueNormalization: ValueNormalizationService

    @Autowired
    lateinit var minMaxSensorValueService: MinMaxSensorValueService

    @Autowired
    lateinit var config: WeatherConfig


    fun currentValuesMap(): Map<SensorEnum, SensorTimeValue?> =

            SensorEnum.values().map { type ->
                val current = sensorDataContainer.sensorsMap[type]!!.current()
                type to (
                        if (current != null)
                            SensorTimeValue(epoch(current.time), if (type != PRESSURE)
                                current.value
                            else
                                pa2hPa(current.value))
                        else null)
            }.toMap()


    // visible for testing
    fun valuesPerTime(): Map<LocalDateTime, Map<SensorEnum, Float>> {

        val firstStepDataMap = mutableMapOf<LocalDateTime, MutableMap<SensorEnum, Float>>()

        SensorEnum.values().forEach { type ->

            sensorDataContainer.sensorsMap[type]!!.values.forEach { entry ->
                var sensorValues = firstStepDataMap[entry.time]
                if (sensorValues == null) {
                    sensorValues = mutableMapOf()
                    firstStepDataMap[entry.time] = sensorValues
                }
                sensorValues[type] = entry.value
            }
        }

        return firstStepDataMap.toMap().mapValues { v -> v.value.toMap() }
    }


    // visible for testing
    fun dataList(valuesPerTime: Map<LocalDateTime, Map<SensorEnum, Float>>): List<List<Number?>> =

            valuesPerTime.keys.sorted().map { time ->

                val entryList = mutableListOf<Number?>(epoch(time))
                SensorEnum.values().forEach { type ->
                    val sensorValues = valuesPerTime[time]
                    if (sensorValues != null) {
                        val sensorTypeValue = sensorValues[type]
                        entryList.add(sensorTypeValue)
                    } else
                        entryList.add(null)
                }
                entryList.toList()
            }


    private fun minMax(): List<PeriodMinMax> {

        fun convert(type: SensorEnum, value: Float) = if (type != PRESSURE) value else pa2hPa(value)

        fun createPeriod(daysBack: Int): PeriodMinMax {

            val minMaxPerType = SensorEnum.values().map { type ->
                type to
                        minMaxSensorValueService.minMax(type, daysBack)
            }.toMap()

            val mins = minMaxPerType.map {
                it.key to
                        if (it.value != null) SensorTimeValue(epoch(it.value!!.minTime), convert(it.key, it.value!!.min)) else null
            }.toMap()

            val maxs = minMaxPerType.map {
                it.key to
                        if (it.value != null) SensorTimeValue(epoch(it.value!!.maxTime), convert(it.key, it.value!!.max)) else null
            }.toMap()

            return PeriodMinMax(daysBack, mins, maxs)
        }

        return config.daysBackMinMax.map { daysBack -> createPeriod(daysBack) }
    }

    class ServiceDataContainer(val order: List<SensorEnum>,
                               val dataList: List<List<Number?>>,
                               val minMax: List<PeriodMinMax>)

    fun data(): ServiceDataContainer {

        val valuesPerTime = valuesPerTime()
        val normalizedValuesPerTime = valueNormalization.normalizeValues(valuesPerTime)
        val dataList = dataList(normalizedValuesPerTime)

        return ServiceDataContainer(values().toList(), dataList, minMax())
    }

    fun activeSensors(): Set<SensorEnum> {

        val lastAcceptableTime = now().minusMinutes(config.downtimeBeforeAltertMinutes)

        var result = SensorEnum.values().toMutableSet()
        valuesPerTime().filterKeys { it >= lastAcceptableTime }.forEach {
            it.value.forEach { result.add(it.key) }
        }
        return result.toSet()
    }
}