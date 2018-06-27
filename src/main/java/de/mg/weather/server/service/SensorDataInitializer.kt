package de.mg.weather.server.service

import de.mg.weather.server.conf.WeatherConfig
import de.mg.weather.server.db.SensorValueRepo
import de.mg.weather.server.model.SensorDataContainer
import de.mg.weather.server.model.SensorDataEntry
import de.mg.weather.server.model.SensorEnum
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class SensorDataInitializer {

    private val log = LoggerFactory.getLogger(SensorDataInitializer::class.java.name)


    @Autowired
    private lateinit var config: WeatherConfig

    @Autowired
    private lateinit var repo: SensorValueRepo

    @Autowired
    private lateinit var sensorDataContainer: SensorDataContainer


    fun initialize() {

        val showSince = LocalDateTime.now().minusHours(config.hoursToShow.toLong())
        val allData = repo.findSince(Utils.epoch(showSince)).toList()

        log.info("loaded from db: ${allData.size} entries, ${repo.count()} present overall")

        SensorEnum.values().forEach { type ->

            val dataList = sensorDataContainer.sensorsMap[type]!!.values

            allData.filter { it.id!!.type == type }.sortedBy { it.id!!.time }.forEach { sensorValueEntity ->

                val localDateTime = Instant.ofEpochMilli(sensorValueEntity.id!!.time!!).atZone(ZoneId.systemDefault()).toLocalDateTime()
                dataList.add(SensorDataEntry(localDateTime, sensorValueEntity.value!!))
            }
        }
    }
}