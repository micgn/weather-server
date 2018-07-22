package de.mg.weather.server.service

import de.mg.weather.server.db.SensorValueEntity
import de.mg.weather.server.model.SensorEnum
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import javax.persistence.EntityManager
import javax.persistence.NoResultException
import javax.persistence.PersistenceContext


@Component
open class MinMaxSensorValueService {

    @PersistenceContext
    private lateinit var em: EntityManager


    fun minMax(type: SensorEnum): MinMax? {


        val maxEntity = findMax(type) ?: return null
        val minEntity = findMin(type) ?: return null

        return MinMax(minEntity.getValue(), Utils.dateTime(minEntity.getId().getTime()),
                maxEntity.getValue(), Utils.dateTime(maxEntity.getId().getTime()))
    }

    class MinMax(val min: Float, val minTime: LocalDateTime,
                 val max: Float, val maxTime: LocalDateTime) {

        fun dist() = max - min
    }

    private fun findMax(type: SensorEnum): SensorValueEntity? = findMinMax(type, "desc")

    private fun findMin(type: SensorEnum): SensorValueEntity? = findMinMax(type, "asc")

    private fun findMinMax(type: SensorEnum, order: String): SensorValueEntity? {
        val query =
                em.createQuery("select s from SensorValueEntity s where s.id.type = :type order by s.value $order")
                        .setParameter("type", type)
                        .setMaxResults(1)

        return try {
            query.singleResult as SensorValueEntity
        } catch (e: NoResultException) {
            null
        }
    }
}