package de.mg.weather.server.service

import de.mg.weather.server.api.SensorTimeValue
import de.mg.weather.server.model.SensorDataContainer
import de.mg.weather.server.model.SensorEnum
import de.mg.weather.server.model.SensorEnum.values
import de.mg.weather.server.service.Utils.epoch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class ApiMapperService {

    @Autowired
    lateinit var sensorDataContainer: SensorDataContainer

    @Autowired
    lateinit var valueNormalization: ValueNormalizationService


    fun currentValuesMap(): Map<SensorEnum, SensorTimeValue?> =

            values().map { type ->
                val current = sensorDataContainer.sensorsMap[type]!!.current()
                type to (if (current != null) SensorTimeValue(epoch(current.time), current.value) else null)
            }.toMap()


    // visible for testing
    fun valuesPerTime(): Map<LocalDateTime, Map<SensorEnum, Float>> {

        val firstStepDataMap = mutableMapOf<LocalDateTime, MutableMap<SensorEnum, Float>>()

        values().forEach { type ->

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
                values().forEach { type ->
                    val sensorValues = valuesPerTime[time]
                    if (sensorValues != null) {
                        val sensorTypeValue = sensorValues[type]
                        entryList.add(sensorTypeValue)
                    } else
                        entryList.add(null)
                }
                entryList.toList()
            }


    class SensorTypeOrderAndDataList(val order: List<SensorEnum>, val dataList: List<List<Number?>>)

    fun data(): SensorTypeOrderAndDataList {

        val valuesPerTime = valuesPerTime()
        val normalizedValuesPerTime = valueNormalization.normalizeValues(valuesPerTime)
        val dataList = dataList(normalizedValuesPerTime)

        return SensorTypeOrderAndDataList(values().toList(), dataList)
    }
}