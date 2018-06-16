package de.mg.weather.server.api

import de.mg.weather.server.model.SensorData
import de.mg.weather.server.model.SensorDataEntry
import de.mg.weather.server.model.SensorEnum
import de.mg.weather.server.service.Utils.epoch
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDateTime

class RestControllerTest {

    private val sut = RestController()

    @Test
    fun currentValues() {

        sut.data = SensorData()
        sut.data.sensorsMap[SensorEnum.TEMPERATURE_1]!!.lastReceived.set(SensorDataEntry(time(0), 1f))
        sut.data.sensorsMap[SensorEnum.TEMPERATURE_2]!!.lastReceived.set(SensorDataEntry(time(1), 2f))

        val result = sut.data().current
        assertThat(result[SensorEnum.TEMPERATURE_1]!!.value).isEqualTo(1f)
        assertThat(result[SensorEnum.TEMPERATURE_2]!!.value).isEqualTo(2f)
        assertThat(result[SensorEnum.PRESSURE]).isNull()
    }

    @Test
    fun valuesEmpty() {

        sut.data = SensorData()
        assertThat(sut.data().data).isEmpty()
    }

    @Test
    fun oneSensorSeveralValues() {

        sut.data = SensorData()

        val values = listOf(
                SensorDataEntry(time(0), 12f),
                SensorDataEntry(time(2), 13f),
                SensorDataEntry(time(4), 14f))

        sut.data.sensorsMap[SensorEnum.TEMPERATURE_1]!!.values.addAll(values)

        val result = sut.data().data
        assertThat(result).hasSize(3)
        assertThat(result[0]).hasSize(SensorEnum.values().size + 1)

        assertThat(result[0][0]).isEqualTo(epoch(time(0)))
        assertThat(result[0][1]).isEqualTo(12L)
        assertThat(result[0][2]).isNull()

        assertThat(result[2][1]).isEqualTo(14L)
    }

    @Test
    fun severalSensors() {

        sut.data = SensorData()

        val temp1Values = listOf(
                SensorDataEntry(time(0), 12f),
                SensorDataEntry(time(2), 13f))

        sut.data.sensorsMap[SensorEnum.TEMPERATURE_1]!!.values.addAll(temp1Values)

        val humiValues = listOf(
                SensorDataEntry(time(0), 100f),
                SensorDataEntry(time(1), 150f))

        sut.data.sensorsMap[SensorEnum.HUMIDITY]!!.values.addAll(humiValues)

        val result = sut.data().data
        assertThat(result).hasSize(3)
        assertThat(result[0]).hasSize(SensorEnum.values().size + 1)

        assertThat(result[0][0]).isEqualTo(epoch(time(0)))
        assertThat(result[0][1]).isEqualTo(12L)
        assertThat(result[0][2]).isNull()
        assertThat(result[0][3]).isEqualTo(100L)
        assertThat(result[0][4]).isNull()

        assertThat(result[1][0]).isEqualTo(epoch(time(1)))
        assertThat(result[1][1]).isNull()
        assertThat(result[1][3]).isEqualTo(150L)

        assertThat(result[2][0]).isEqualTo(epoch(time(2)))
        assertThat(result[2][1]).isEqualTo(13L)
        assertThat(result[2][3]).isNull()
    }

    private fun time(min: Int) = LocalDateTime.of(2018, 6, 16, 14, min, 0)

}