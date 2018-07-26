package de.mg.weather.server.api

import de.mg.weather.server.model.SensorEnum


class ApiData(
        val series: List<SensorEnum>,

        /** first list = x axis, second list = x value and y values for all series **/
        val data: List<List<Number?>>,

        val current: Map<SensorEnum, SensorTimeValue?>)

class SensorTimeValue(val time: Long, val value: Float)