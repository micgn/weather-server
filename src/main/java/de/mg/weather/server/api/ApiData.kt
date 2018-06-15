package de.mg.weather.server.api

import de.mg.weather.server.model.SensorEnum

class ApiData(val series: List<SensorEnum>, val data: List<List<Long>>, current: Map<SensorEnum, SensorTimeValue>)

class SensorTimeValue(time: Long, value: Long)