package de.mg.weather.server.model

import de.mg.weather.server.db.SensorEnum
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicReference


class SensorTypeData(val type: SensorEnum) {

    val lastReceived = AtomicReference<SensorDataEntry>()

    val values = ConcurrentLinkedQueue<SensorDataEntry>()

}

