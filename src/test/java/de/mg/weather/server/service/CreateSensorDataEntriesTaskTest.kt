package de.mg.weather.server.service

import de.mg.weather.server.conf.WeatherConfig
import de.mg.weather.server.db.SensorValueRepo
import de.mg.weather.server.model.SensorDataContainer
import de.mg.weather.server.model.SensorDataEntry
import de.mg.weather.server.model.SensorEnum.TEMPERATURE_1
import de.mg.weather.server.model.SensorTypeData
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import java.time.LocalDateTime
import java.util.*


@RunWith(MockitoJUnitRunner::class)
open class CreateSensorDataEntriesTaskTest {

    @InjectMocks
    private lateinit var sut: CreateSensorDataEntriesTask

    @Mock
    private lateinit var config: WeatherConfig

    @Mock
    private lateinit var repo: SensorValueRepo

    @Before
    fun init() {
        sut.sensorDataContainer = SensorDataContainer()
        sut.sensorDataContainer.sensorTimeDistanceMinutes = 2
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


    @Test
    fun normalizeTimestamp() {

        val tz = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

        var time = LocalDateTime.of(1970, 1, 1, 0, 0, 0)
        var result = sut.normalizeTimestamp(time, 1)
        assertThat(result).isEqualTo(time)

        time = LocalDateTime.of(1970, 1, 1, 0, 1, 30)
        result = sut.normalizeTimestamp(time, 1)
        assertThat(result).isEqualTo(LocalDateTime.of(1970, 1, 1, 0, 1, 0))

        time = LocalDateTime.of(1970, 1, 1, 0, 1, 30)
        result = sut.normalizeTimestamp(time, 2)
        assertThat(result).isEqualTo(LocalDateTime.of(1970, 1, 1, 0, 0, 0))

        time = LocalDateTime.of(1970, 1, 1, 0, 3, 0)
        result = sut.normalizeTimestamp(time, 2)
        assertThat(result).isEqualTo(LocalDateTime.of(1970, 1, 1, 0, 2, 0))

        time = LocalDateTime.of(1970, 1, 1, 0, 4, 30)
        result = sut.normalizeTimestamp(time, 2)
        assertThat(result).isEqualTo(LocalDateTime.of(1970, 1, 1, 0, 4, 0))

        time = LocalDateTime.of(2018, 6, 23, 11, 15, 30)
        result = sut.normalizeTimestamp(time, 2)
        assertThat(result).isEqualTo(LocalDateTime.of(2018, 6, 23, 11, 14, 0))

        time = LocalDateTime.of(2018, 6, 23, 11, 10, 30)
        result = sut.normalizeTimestamp(time, 3)
        assertThat(result).isEqualTo(LocalDateTime.of(2018, 6, 23, 11, 9, 0))

        TimeZone.setDefault(tz)
    }


    private fun time(min: Int) = LocalDateTime.of(2018, 6, 15, 18, min, 0)

}