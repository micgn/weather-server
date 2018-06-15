package de.mg.weather.server.service

import de.mg.weather.server.model.SensorData
import de.mg.weather.server.model.SensorDataEntry
import de.mg.weather.server.model.SensorEnum.TEMPERATURE_1
import de.mg.weather.server.model.SensorTypeData
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class CreateSensorDataEntriesTaskTest {

    private val sut = CreateSensorDataEntriesTask()

    @Before
    fun init() {
        sut.data = SensorData()
    }

    @Test
    fun testInterpolation() {

        val result = sut.getInterpolatedValue(
                lastNormalized = SensorDataEntry(time(0), 20f),
                lastReceived = SensorDataEntry(time(5), 40f),
                requiredTime = time(10))

        assertEquals(60f, result)

        val result2 = sut.getInterpolatedValue(
                lastNormalized = SensorDataEntry(time(0), 10f),
                lastReceived = SensorDataEntry(time(2), 20f),
                requiredTime = time(6))

        assertEquals(40f, result2)

    }


    @Test
    fun addNormalizedValuesToNotEmptyList_noNewEntry() {

        val sensorTypeData = SensorTypeData(TEMPERATURE_1)
        val lastReceived = SensorDataEntry(time(1), 40f)
        sensorTypeData.lastReceived.set(lastReceived)

        sut.addNormalizedValuesToNotEmptyList(
                lastNormalizedEntry = SensorDataEntry(time(0), 10f),
                lastReceived = lastReceived,
                sensorTypeData = sensorTypeData,
                type = TEMPERATURE_1)

        assertNotNull(sensorTypeData.lastReceived.get())
        assertEquals(0, sensorTypeData.values.size)
    }

    @Test
    fun addNormalizedValuesToNotEmptyList_oneNewEntry() {

        val sensorTypeData = SensorTypeData(TEMPERATURE_1)
        val lastReceived = SensorDataEntry(time(3), 30f)
        sensorTypeData.lastReceived.set(lastReceived)

        sut.addNormalizedValuesToNotEmptyList(
                lastNormalizedEntry = SensorDataEntry(time(0), 0f),
                lastReceived = lastReceived,
                sensorTypeData = sensorTypeData,
                type = TEMPERATURE_1)

        assertNull(sensorTypeData.lastReceived.get())
        assertEquals(1, sensorTypeData.values.size)
        assertEquals(2, sensorTypeData.values.first.time.minute)
        assertEquals(20f, sensorTypeData.values.first.value)
    }

    @Test
    fun addNormalizedValuesToNotEmptyList_severalNewEntries() {

        val sensorTypeData = SensorTypeData(TEMPERATURE_1)
        val lastReceived = SensorDataEntry(time(7), 40f)
        sensorTypeData.lastReceived.set(lastReceived)

        sut.addNormalizedValuesToNotEmptyList(
                lastNormalizedEntry = SensorDataEntry(time(0), 10f),
                lastReceived = lastReceived,
                sensorTypeData = sensorTypeData,
                type = TEMPERATURE_1)

        assertNull(sensorTypeData.lastReceived.get())
        assertEquals(3, sensorTypeData.values.size)
        assertEquals(2, sensorTypeData.values.first.time.minute)
        assertEquals(6, sensorTypeData.values.last.time.minute)
    }


    private fun time(min: Int) = LocalDateTime.of(2018, 6, 15, 18, min, 0)

}