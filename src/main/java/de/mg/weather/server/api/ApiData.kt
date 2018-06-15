package de.mg.weather.server.api

import de.mg.weather.server.model.SensorEnum

class ApiData(
        val series: List<SensorEnum>,

        /** first list = x axis, second list = x value and y values for all series **/
        val data: List<List<Long>>,

        current: Map<SensorEnum, SensorTimeValue>)

class SensorTimeValue(time: Long, value: Float)