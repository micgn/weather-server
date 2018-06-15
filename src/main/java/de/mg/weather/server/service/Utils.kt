package de.mg.weather.server.service

import java.time.LocalDateTime
import java.time.ZoneId

object Utils {

    fun epoch(ldt: LocalDateTime) = ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}