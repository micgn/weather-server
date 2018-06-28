package de.mg.weather.server.service

import de.mg.weather.server.conf.WeatherConfig
import de.mg.weather.server.db.SensorValueRepo
import de.mg.weather.server.model.SensorEnum
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class OriginalDataService {

    @Autowired
    private lateinit var config: WeatherConfig

    @Autowired
    private lateinit var repo: SensorValueRepo

    fun createCsv(type: SensorEnum): String {

        val allData = repo.findSince(Utils.epoch(config.showSince())).toList()

        var result = ""
        allData.filter { it.id!!.type == type }.sortedBy { it.id!!.time }.forEach {
            result += Utils.dateTime(it.id!!.time!!).toString() + ";"
            result += it.value.toString() + "\n"
        }
        return result
    }
}