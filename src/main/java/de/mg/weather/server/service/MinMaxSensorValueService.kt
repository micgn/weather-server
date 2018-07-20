package de.mg.weather.server.service

import de.mg.weather.server.db.SensorValueRepo
import de.mg.weather.server.model.SensorEnum
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
open class MinMaxSensorValueService {

    @Autowired
    lateinit var repo: SensorValueRepo

    fun minMax(type: SensorEnum): MinMax? {

        val maxEntity = repo.findMaxValue(type) ?: return null
        val minEntity = repo.findMinValue(type) ?: return null

        return MinMax(minEntity.value!!, Utils.dateTime(minEntity.id!!.time)!!,
                maxEntity.value!!, Utils.dateTime(maxEntity.id!!.time)!!)
    }

    class MinMax(val min: Float, val minTime: LocalDateTime,
                 val max: Float, val maxTime: LocalDateTime) {

        fun dist() = max - min

        fun getMerged(other: MinMax?): MinMax =

                if (other == null)
                    MinMax(min, minTime, max, maxTime)
                else
                    MinMax(
                            if (this.min < other.min) this.min else other.min,
                            if (this.min < other.min) this.minTime else other.minTime,
                            if (this.max > other.max) this.max else other.max,
                            if (this.max > other.max) this.maxTime else other.maxTime
                    )

    }
}