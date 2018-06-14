package de.mg.weather.server.service

import org.springframework.scheduling.annotation.Scheduled

class ScheduledTask {

    @Scheduled(cron = "0 * * ? * *" /* every minute */)
    fun run() {


    }
}