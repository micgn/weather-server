package de.mg.weather.server.service

import de.mg.weather.server.db.SensorEnum
import de.mg.weather.server.model.SensorData
import de.mg.weather.server.model.SensorDataEntry
import de.mg.weather.server.model.SensorTypeData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class ScheduledTask {

    @Autowired
    lateinit var data: SensorData

    @Scheduled(cron = "0 * * ? * *" /* every minute */)
    fun run() {

        SensorEnum.values().forEach {
            normalizeSensorData(it)
        }
    }

    private fun normalizeSensorData(it: SensorEnum) {

        val sensorData = data.sensorsMap[it]!!

        val lastReceived = sensorData.lastReceived.get() ?: return

        val lastNormalized = sensorData.values.last()
        if (lastNormalized == null)
            addNormalizedEntryToEmptyList(lastReceived, sensorData)
        else
            addNormalizedValuesToNotEmptyList(lastNormalized, lastReceived, sensorData)

    }

    private fun addNormalizedEntryToEmptyList(lastReceived: SensorDataEntry, sensorData: SensorTypeData) {

        val lastTime = lastReceived.time
        val nextRequiredTime = LocalDateTime.of(lastTime.year, lastTime.month, lastTime.dayOfMonth, lastTime.hour, lastTime.minute, 0, 0)
        sensorData.values.add(SensorDataEntry(nextRequiredTime, lastReceived.value))
        sensorData.lastReceived.set(null)
    }

    // visible for testing
    fun addNormalizedValuesToNotEmptyList(lastNormalizedEntry: SensorDataEntry, lastReceived: SensorDataEntry, sensorTypeData: SensorTypeData) {

        var lastNormalized = lastNormalizedEntry
        var nextRequiredTime = lastNormalized.time.plusMinutes(data.sensorTimeDistanceMinutes)
        var lastReceivedUsed = false

        while (nextRequiredTime.isBefore(lastReceived.time)) {

            val interpolatedValue = getInterpolatedValue(nextRequiredTime, lastNormalized, lastReceived)
            val newEntry = SensorDataEntry(nextRequiredTime, interpolatedValue)
            sensorTypeData.values.add(newEntry)
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


    private fun epoch(ldt: LocalDateTime) = ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}