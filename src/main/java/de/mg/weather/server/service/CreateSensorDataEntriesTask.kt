package de.mg.weather.server.service

import de.mg.weather.server.conf.WeatherConfig
import de.mg.weather.server.db.SensorValueEntity
import de.mg.weather.server.db.SensorValueIdEntity
import de.mg.weather.server.db.SensorValueRepo
import de.mg.weather.server.model.SensorData
import de.mg.weather.server.model.SensorDataEntry
import de.mg.weather.server.model.SensorEnum
import de.mg.weather.server.model.SensorTypeData
import de.mg.weather.server.service.Utils.epoch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class CreateSensorDataEntriesTask {

    @Autowired
    lateinit var config: WeatherConfig

    @Autowired
    lateinit var data: SensorData

    @Autowired
    lateinit var repo: SensorValueRepo


    @Scheduled(cron = "0 * * ? * *" /* every minute */)
    fun run() {

        SensorEnum.values().forEach {
            normalizeSensorData(it)
        }
    }

    private fun normalizeSensorData(type: SensorEnum) {

        val sensorData = data.sensorsMap[type]!!

        val lastReceived = sensorData.lastReceived.get() ?: return

        val lastNormalized = sensorData.values.last()
        if (lastNormalized == null || lastNormalizesTooOld(lastNormalized.time, lastReceived.time))
        // TODO add in correct distance!!
            addNormalizedEntryToEmptyList(lastReceived, sensorData, type)
        else
            addNormalizedValuesToNotEmptyList(lastNormalized, lastReceived, sensorData, type)
    }

    private fun lastNormalizesTooOld(lastNormalized: LocalDateTime, lastReceived: LocalDateTime) =
            (epoch(lastReceived) - epoch(lastNormalized)) / 1000 / 60 > data.sensorTimeDistanceMinutes * 2

    private fun addNormalizedEntryToEmptyList(lastReceived: SensorDataEntry, sensorTypeData: SensorTypeData, type: SensorEnum) {

        val lastTime = lastReceived.time
        val nextRequiredTime = LocalDateTime.of(lastTime.year, lastTime.month, lastTime.dayOfMonth, lastTime.hour, lastTime.minute, 0, 0)
        addNewEntry(nextRequiredTime, lastReceived.value, sensorTypeData, type)
        sensorTypeData.lastReceived.set(null)
    }

    // visible for testing
    fun addNormalizedValuesToNotEmptyList(lastNormalizedEntry: SensorDataEntry, lastReceived: SensorDataEntry,
                                          sensorTypeData: SensorTypeData, type: SensorEnum) {

        var lastNormalized = lastNormalizedEntry
        var nextRequiredTime = lastNormalized.time.plusMinutes(data.sensorTimeDistanceMinutes)
        var lastReceivedUsed = false

        while (nextRequiredTime.isBefore(lastReceived.time)) {

            val interpolatedValue = getInterpolatedValue(nextRequiredTime, lastNormalized, lastReceived)
            val newEntry = addNewEntry(nextRequiredTime, interpolatedValue, sensorTypeData, type)
            lastReceivedUsed = true

            lastNormalized = newEntry
            nextRequiredTime = lastNormalized.time.plusMinutes(data.sensorTimeDistanceMinutes)
        }

        if (lastReceivedUsed) sensorTypeData.lastReceived.set(null)
    }

    // visible for testing
    fun getInterpolatedValue(requiredTime: LocalDateTime, lastNormalized: SensorDataEntry, lastReceived: SensorDataEntry): Float =

            (epoch(requiredTime) - epoch(lastNormalized.time)) *
                    (lastReceived.value - lastNormalized.value) /
                    (epoch(lastReceived.time) - epoch(lastNormalized.time)) +
                    lastNormalized.value


    private fun addNewEntry(time: LocalDateTime, value: Float, sensorTypeData: SensorTypeData, sensorType: SensorEnum): SensorDataEntry {

        val newEntry = SensorDataEntry(time, value)
        sensorTypeData.values.add(newEntry)

        // save to db
        val id = SensorValueIdEntity()
        id.type = sensorType
        id.time = epoch(time)
        val entity = SensorValueEntity()
        entity.id = id
        entity.value = value
        repo.save(entity)

        removeOldEntiesFromMemory(sensorTypeData)

        return newEntry
    }

    private fun removeOldEntiesFromMemory(sensorTypeData: SensorTypeData) {

        val removeBefore = LocalDateTime.now().minusHours(config.hoursToShow.toLong())
        sensorTypeData.values.removeIf { it.time.isBefore(removeBefore) }
    }

}