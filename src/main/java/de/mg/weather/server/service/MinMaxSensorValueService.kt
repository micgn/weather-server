package de.mg.weather.server.service

import de.mg.weather.server.db.SensorValueEntity
import de.mg.weather.server.model.SensorEnum
import de.mg.weather.server.service.Utils.epoch
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import javax.persistence.EntityManager
import javax.persistence.NoResultException
import javax.persistence.PersistenceContext


@Component
open class MinMaxSensorValueService {

    @PersistenceContext
    private lateinit var em: EntityManager


    fun minMax(type: SensorEnum, daysBack: Int): MinMax? {


        val maxEntity = findMax(type, daysBack) ?: return null
        val minEntity = findMin(type, daysBack) ?: return null

        return MinMax(minEntity.getValue(), Utils.dateTime(minEntity.getId().getTime()),
                maxEntity.getValue(), Utils.dateTime(maxEntity.getId().getTime()))
    }

    class MinMax(val min: Float, val minTime: LocalDateTime,
                 val max: Float, val maxTime: LocalDateTime) {

        fun dist() = max - min
    }

    private fun findMax(type: SensorEnum, daysBack: Int): SensorValueEntity? = findMinMax(type, "desc", daysBack)

    private fun findMin(type: SensorEnum, daysBack: Int): SensorValueEntity? = findMinMax(type, "asc", daysBack)

    private fun findMinMax(type: SensorEnum, order: String, daysBack: Int): SensorValueEntity? {

        val startTime = epoch(LocalDateTime.now().minusDays(daysBack.toLong()))

        val query =
                em.createQuery("select s from SensorValueEntity s where s.id.type = :type and s.id.time >= :startTime order by s.value $order")
                        .setParameter("type", type)
                        .setParameter("startTime", startTime)
                        .setMaxResults(1)

        return try {
            query.singleResult as SensorValueEntity
        } catch (e: NoResultException) {
            null
        }
    }
}