package de.mg.weather.server.model

import de.mg.weather.server.db.SensorEnum
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class SensorData {

    val data: MutableList<SensorDataEntry> = arrayListOf()


}

class SensorDataEntry(val type: SensorEnum, val time: LocalDateTime, val value: Float) {

}