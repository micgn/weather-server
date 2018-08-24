package de.mg.weather.server.service

import de.mg.weather.server.db.SensorValueRepo
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
open class ManageDataService {

    private val log = LoggerFactory.getLogger(ManageDataService::class.java.name)


    @Autowired
    lateinit var repo: SensorValueRepo

    @Transactional
    open fun deleteOld(keepHours: Int) {

        val time = LocalDateTime.now().minusHours(keepHours.toLong())
        val epochTime = Utils.epoch(time)
        val toDelete = repo.findBefore(epochTime)
        repo.delete(toDelete)
        log.warn("deleted ${toDelete.size} entries on user request, keeping last $keepHours hours")
    }

}