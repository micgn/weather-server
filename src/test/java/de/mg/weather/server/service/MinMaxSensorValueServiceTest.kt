package de.mg.weather.server.service

import de.mg.weather.server.db.SensorValueEntity
import de.mg.weather.server.db.SensorValueIdEntity
import de.mg.weather.server.db.SensorValueRepo
import de.mg.weather.server.model.SensorEnum
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest
@ActiveProfiles("test")
class MinMaxSensorValueServiceTest {

    @Autowired
    private lateinit var sut: MinMaxSensorValueService

    @Autowired
    private lateinit var repo: SensorValueRepo


    @Test
    fun testEmptyDB() {
        assertThat(sut.minMax(SensorEnum.PRESSURE, 5)).isNull()
    }

    @Test
    fun test() {
        save(SensorEnum.PRESSURE, 100f, 1)
        save(SensorEnum.PRESSURE, 500f, 2)
        save(SensorEnum.PRESSURE, 1000f, 3)
        save(SensorEnum.HUMIDITY, 0f, 4)
        save(SensorEnum.TEMPERATURE_1, 20f, 5)
        assertThat(repo.count()).isEqualTo(5)

        val result = sut.minMax(SensorEnum.PRESSURE, 999999)
        assertThat(result).isNotNull()
        assertThat(result!!.min).isEqualTo(1f)
        assertThat(result.max).isEqualTo(10f)
    }


    private fun save(type: SensorEnum, value: Float, time: Long) {
        val entity = SensorValueEntity()
        entity.setValue(value)
        val id = SensorValueIdEntity()
        id.setTime(time)
        id.setType(type)
        entity.setId(id)
        repo.saveAndFlush(entity)
    }
}