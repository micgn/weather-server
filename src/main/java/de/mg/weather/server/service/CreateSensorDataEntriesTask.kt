package de.mg.weather.server.service

import de.mg.weather.server.conf.WeatherConfig
import de.mg.weather.server.db.SensorValueEntity
import de.mg.weather.server.db.SensorValueIdEntity
import de.mg.weather.server.db.SensorValueRepo
import de.mg.weather.server.model.SensorDataContainer
import de.mg.weather.server.model.SensorDataEntry
import de.mg.weather.server.model.SensorEnum
import de.mg.weather.server.model.SensorTypeData
import de.mg.weather.server.service.Utils.dateTime
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
    lateinit var sensorDataContainer: SensorDataContainer

    @Autowired
    lateinit var repo: SensorValueRepo


    @Scheduled(cron = "0 * * ? * *" /* every minute */)
    fun run() {

        SensorEnum.values().forEach {
            createNormalizeSensorData(it)
            removeOldEntriesFromMemory(it)
        }
    }

    private fun createNormalizeSensorData(type: SensorEnum) {

        val sensorData = sensorDataContainer.sensorsMap[type]!!

        val lastReceived = sensorData.lastReceived.get() ?: return

        val lastNormalized = sensorData.values.peekLast()
        if (lastNormalized == null || isLastNormalizedTimeTooOld(lastNormalized.time, lastReceived.time))
            addNormalizedEntryToEmptyList(lastReceived, sensorData, type)
        else
            addNormalizedValuesToNotEmptyList(lastNormalized, lastReceived, sensorData, type)
    }

    private fun isLastNormalizedTimeTooOld(lastNormalized: LocalDateTime, lastReceived: LocalDateTime) =
            (epoch(lastReceived) - epoch(lastNormalized)) / 1000 / 60 > sensorDataContainer.sensorTimeDistanceMinutes * 2

    private fun addNormalizedEntryToEmptyList(lastReceived: SensorDataEntry, sensorTypeData: SensorTypeData, type: SensorEnum) {

        val lastTime = lastReceived.time
        val nextRequiredTime = normalizeTimestamp(lastTime, sensorDataContainer.sensorTimeDistanceMinutes)
        addNewEntry(nextRequiredTime, lastReceived.value, sensorTypeData, type)
        sensorTypeData.lastReceived.set(null)
    }

    // visible for testing
    fun normalizeTimestamp(time: LocalDateTime, sensorTimeDistanceMinutes: Long): LocalDateTime {

        val zeroSecondsTime = LocalDateTime.of(time.year, time.month, time.dayOfMonth, time.hour, time.minute, 0, 0)
        val zeroSecondsEpoch = epoch(zeroSecondsTime)
        val minuteTimeEpoch = zeroSecondsEpoch / 1000 / 60
        val normalizedEpoch = zeroSecondsEpoch - (minuteTimeEpoch % sensorTimeDistanceMinutes) * 1000 * 60
        return dateTime(normalizedEpoch)
    }

    // visible for testing
    fun addNormalizedValuesToNotEmptyList(lastNormalizedEntry: SensorDataEntry, lastReceived: SensorDataEntry,
                                          sensorTypeData: SensorTypeData, type: SensorEnum) {

        var lastNormalized = lastNormalizedEntry
        var nextRequiredTime = lastNormalized.time.plusMinutes(sensorDataContainer.sensorTimeDistanceMinutes)
        var lastReceivedUsed = false

        while (nextRequiredTime.isBefore(lastReceived.time)) {

            val interpolatedValue = getInterpolatedValue(nextRequiredTime, lastNormalized, lastReceived)
            val newEntry = addNewEntry(nextRequiredTime, interpolatedValue, sensorTypeData, type)
            lastReceivedUsed = true

            lastNormalized = newEntry
            nextRequiredTime = lastNormalized.time.plusMinutes(sensorDataContainer.sensorTimeDistanceMinutes)
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
        id.setType(sensorType)
        id.setTime(epoch(time))
        val entity = SensorValueEntity()
        entity.setId(id)
        entity.setValue(value)
        repo.save(entity)

        return newEntry
    }

    private fun removeOldEntriesFromMemory(sensorType: SensorEnum) {

        val sensorTypeData = sensorDataContainer.sensorsMap[sensorType]!!
        val removeBefore = LocalDateTime.now().minusHours(config.hoursToShow.toLong())
        sensorTypeData.values.removeIf { it.time.isBefore(removeBefore) }
    }

}