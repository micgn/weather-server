package de.mg.weather.server.service

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId


object Utils {

    fun epoch(ldt: LocalDateTime) = ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    fun epoch(ldt: LocalDateTime?) = if (ldt != null) epoch(ldt) else null


    fun dateTime(epoch: Long) = Instant.ofEpochMilli(epoch).atZone(ZoneId.systemDefault()).toLocalDateTime()!!
    fun dateTime(epoch: Long?) = if (epoch != null) dateTime(epoch) else null
}