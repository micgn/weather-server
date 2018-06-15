package de.mg.weather.server.api


import de.mg.weather.server.model.SensorData
import de.mg.weather.server.model.SensorEnum
import de.mg.weather.server.service.Utils.epoch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import java.time.LocalDateTime

@org.springframework.web.bind.annotation.RestController
class RestController {

    @Autowired
    private lateinit var sensorData: SensorData


    @Suppress("FoldInitializerAndIfToElvis")
    @RequestMapping(value = "/data", produces = ["application/json"])
    fun monthlySums(): ApiData {

        val currentValuesMap = SensorEnum.values().map { type ->
            val last = sensorData.sensorsMap[type]!!.lastReceived.get()
            type to SensorTimeValue(epoch(last.time), last.value)
        }.toMap()


        val dataMap = mutableMapOf<LocalDateTime, MutableMap<SensorEnum, Float>>()
        SensorEnum.values().forEach { type ->

            sensorData.sensorsMap[type]!!.values.forEach { entry ->
                var sensorValues = dataMap[entry.time]
                if (sensorValues == null) {
                    sensorValues = mutableMapOf()
                    dataMap[entry.time] = sensorValues
                }
                sensorValues[type] = entry.value
            }
        }


        val dataList = dataMap.keys.sorted().map { time ->

            val entryList = mutableListOf(epoch(time))
            SensorEnum.values().forEach { type ->
                val sensorValues = dataMap[time]
                if (sensorValues != null) {
                    val sensorTypeValue = sensorValues[type]?.toLong()
                    if (sensorTypeValue != null)
                        entryList.add(sensorTypeValue)
                }
            }
            entryList.toList()
        }

        return ApiData(SensorEnum.values().toList(), dataList, currentValuesMap)
    }


}
