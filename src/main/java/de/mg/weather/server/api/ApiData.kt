package de.mg.weather.server.api

import de.mg.weather.server.model.SensorEnum


class ApiData(
        val series: List<SensorEnum>,

        /** first list = x axis, second list = x value and y values for all series **/
        val data: List<List<Number?>>,

        val current: Map<SensorEnum, SensorTimeValue?>,

        val minMax: List<PeriodMinMax>)

class SensorTimeValue(val time: Long, val value: Float)

class PeriodMinMax(val daysBack: Int,
                   val min: Map<SensorEnum, SensorTimeValue?>,
                   val max: Map<SensorEnum, SensorTimeValue?>)