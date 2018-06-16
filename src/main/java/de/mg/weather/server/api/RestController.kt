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
    lateinit var data: SensorData


    @Suppress("FoldInitializerAndIfToElvis")
    @RequestMapping(value = "/data", produces = ["application/json"])
    fun data(): ApiData {

        val currentValuesMap = SensorEnum.values().map { type ->
            val last = data.sensorsMap[type]!!.lastReceived.get()
            if (last != null)
                type to SensorTimeValue(epoch(last.time), last.value)
            else
                type to null
        }.toMap()


        val dataMap = mutableMapOf<LocalDateTime, MutableMap<SensorEnum, Float>>()
        SensorEnum.values().forEach { type ->

            data.sensorsMap[type]!!.values.forEach { entry ->
                var sensorValues = dataMap[entry.time]
                if (sensorValues == null) {
                    sensorValues = mutableMapOf()
                    dataMap[entry.time] = sensorValues
                }
                sensorValues[type] = entry.value
            }
        }


        val dataList = dataMap.keys.sorted().map { time ->

            val entryList = mutableListOf<Long?>(epoch(time))
            SensorEnum.values().forEach { type ->
                val sensorValues = dataMap[time]
                if (sensorValues != null) {
                    val sensorTypeValue = sensorValues[type]?.toLong()
                    entryList.add(sensorTypeValue)
                } else
                    entryList.add(null)
            }
            entryList.toList()
        }

        return ApiData(SensorEnum.values().toList(), dataList, currentValuesMap)
    }


}
