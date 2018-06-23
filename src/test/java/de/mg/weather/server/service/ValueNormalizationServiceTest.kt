package de.mg.weather.server.service

import de.mg.weather.server.model.SensorEnum.*
import de.mg.weather.server.service.Utils.dateTime
import de.mg.weather.server.service.ValueNormalizationService.Range
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class ValueNormalizationServiceTest {

    private val sut = ValueNormalizationService()

    @Test
    fun singleValueNormalization() {

        var result = sut.normalizeValue(10f, null, null)
        assertThat(result).isEqualTo(10f)


        result = sut.normalizeValue(10f, Range(10f, 10f), Range(20f, 20f))
        assertThat(result).isEqualTo(20f)

        result = sut.normalizeValue(10f, Range(10f, 10f), Range(20f, 30f))
        assertThat(result).isEqualTo(20f)

        result = sut.normalizeValue(10f, Range(10f, 100f), Range(20f, 20f))
        assertThat(result).isEqualTo(20f)


        result = sut.normalizeValue(10f, Range(10f, 30f), Range(20f, 50f))
        assertThat(result).isEqualTo(20f)

        result = sut.normalizeValue(30f, Range(10f, 30f), Range(20f, 50f))
        assertThat(result).isEqualTo(50f)

        result = sut.normalizeValue(20f, Range(10f, 30f), Range(20f, 50f))
        assertThat(result).isEqualTo(35f)


        result = sut.normalizeValue(10f, Range(10f, 30f), Range(-20f, 20f))
        assertThat(result).isEqualTo(-20f)

        result = sut.normalizeValue(20f, Range(10f, 30f), Range(-20f, 20f))
        assertThat(result).isEqualTo(0f)
    }

    @Test
    fun mapNormalization() {

        var result = sut.normalizeValues(emptyMap())
        assertThat(result).isEmpty()

        val list = mapOf(
                dateTime(10) to mapOf(
                        TEMPERATURE_1 to 0f,
                        PRESSURE to 200f,
                        HUMIDITY to 1000f),

                dateTime(20) to mapOf(
                        TEMPERATURE_1 to 30f,
                        PRESSURE to 100f,
                        HUMIDITY to 2000f)
        )

        result = sut.normalizeValues(list)

        assertThat(result[dateTime(10)]!![TEMPERATURE_1]).isEqualTo(0f)
        assertThat(result[dateTime(20)]!![TEMPERATURE_1]).isEqualTo(30f)

        assertThat(result[dateTime(10)]!![PRESSURE]).isEqualTo(30f)
        assertThat(result[dateTime(20)]!![PRESSURE]).isEqualTo(0f)

        assertThat(result[dateTime(10)]!![HUMIDITY]).isEqualTo(0f)
        assertThat(result[dateTime(20)]!![HUMIDITY]).isEqualTo(30f)


    }
}