package de.mg.weather.server.db

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository


@Repository
interface SensorValueRepo : JpaRepository<SensorValue, SensorValueId>

