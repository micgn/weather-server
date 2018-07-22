package de.mg.weather.server.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDateTime.now

class ValueNormalizationServiceTest {


    private var sut = ValueNormalizationService()


    @Test
    fun singleValueNormalization() {

        var result = sut.normalizeValue(10f, null, null)
        assertThat(result).isEqualTo(10f)


        result = sut.normalizeValue(10f, range(10f, 10f), ValueNormalizationService.Range(20f, 20f))
        assertThat(result).isEqualTo(20f)

        result = sut.normalizeValue(10f, range(10f, 10f), ValueNormalizationService.Range(20f, 30f))
        assertThat(result).isEqualTo(20f)

        result = sut.normalizeValue(10f, range(10f, 100f), ValueNormalizationService.Range(20f, 20f))
        assertThat(result).isEqualTo(20f)


        result = sut.normalizeValue(10f, range(10f, 30f), ValueNormalizationService.Range(20f, 50f))
        assertThat(result).isEqualTo(20f)

        result = sut.normalizeValue(30f, range(10f, 30f), ValueNormalizationService.Range(20f, 50f))
        assertThat(result).isEqualTo(50f)

        result = sut.normalizeValue(20f, range(10f, 30f), ValueNormalizationService.Range(20f, 50f))
        assertThat(result).isEqualTo(35f)


        result = sut.normalizeValue(10f, range(10f, 30f), ValueNormalizationService.Range(-20f, 20f))
        assertThat(result).isEqualTo(-20f)

        result = sut.normalizeValue(20f, range(10f, 30f), ValueNormalizationService.Range(-20f, 20f))
        assertThat(result).isEqualTo(0f)
    }

    private fun range(min: Float, max: Float) = MinMaxSensorValueService.MinMax(min, now(), max, now())

}