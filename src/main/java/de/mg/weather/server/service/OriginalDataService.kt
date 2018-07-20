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
        allData.filter { it.getId().getType() == type }.sortedBy { it.getId().getTime() }.forEach {
            result += Utils.dateTime(it.getId().getTime()).toString() + ";"
            result += it.getValue().toString() + "\n"
        }
        return result
    }
}