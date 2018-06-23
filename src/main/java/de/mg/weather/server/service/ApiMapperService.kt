package de.mg.weather.server.service

import de.mg.weather.server.api.SensorTimeValue
import de.mg.weather.server.model.SensorDataContainer
import de.mg.weather.server.model.SensorEnum
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class ApiMapperService {

    @Autowired
    lateinit var sensorDataContainer: SensorDataContainer


    fun currentValuesMap(): Map<SensorEnum, SensorTimeValue?> =

            SensorEnum.values().map { type ->
                val last = sensorDataContainer.sensorsMap[type]!!.lastReceived.get()
                if (last != null)
                    type to SensorTimeValue(Utils.epoch(last.time), last.value)
                else
                    type to null
            }.toMap()


    // visible for testing
    fun valuesPerTime(): Map<LocalDateTime, MutableMap<SensorEnum, Float>> {

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

        return firstStepDataMap.toMap()
    }

    // visible for tesing
    fun dataList(valuesPerTime: Map<LocalDateTime, MutableMap<SensorEnum, Float>>): List<List<Long?>> =

            valuesPerTime.keys.sorted().map { time ->

                val entryList = mutableListOf<Long?>(Utils.epoch(time))
                SensorEnum.values().forEach { type ->
                    val sensorValues = valuesPerTime[time]
                    if (sensorValues != null) {
                        val sensorTypeValue = sensorValues[type]?.toLong()
                        entryList.add(sensorTypeValue)
                    } else
                        entryList.add(null)
                }
                entryList.toList()
            }


    fun data(): SensorTypeOrderAndDataList =
            SensorTypeOrderAndDataList(SensorEnum.values().toList(), dataList(valuesPerTime()))


    class SensorTypeOrderAndDataList(val order: List<SensorEnum>, val dataList: List<List<Long?>>)
}