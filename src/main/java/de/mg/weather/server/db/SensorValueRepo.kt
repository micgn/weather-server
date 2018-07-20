package de.mg.weather.server.db

import de.mg.weather.server.model.SensorEnum
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository


@Repository
interface SensorValueRepo : JpaRepository<SensorValueEntity, SensorValueIdEntity> {

    @Query("select s from SensorValueEntity s where s.id.time >= :since")
    fun findSince(@Param("since") since: Long): List<SensorValueEntity>

    @Query("select max(s.value) from SensorValueEntity s where s.id.type = :type")
    fun findMaxValue(@Param("type") type: SensorEnum): SensorValueEntity?

    @Query("select min(s.value) from SensorValueEntity s where s.id.type = :type")
    fun findMinValue(@Param("type") type: SensorEnum): SensorValueEntity?
}

