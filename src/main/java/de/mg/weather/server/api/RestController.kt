package de.mg.weather.server.api


import de.mg.weather.server.conf.WeatherConfig
import de.mg.weather.server.service.ApiMapperService
import de.mg.weather.server.service.ManageDataService
import de.mg.weather.server.service.OriginalDataService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam


@org.springframework.web.bind.annotation.RestController
class RestController {

    @Autowired
    private lateinit var config: WeatherConfig

    @Autowired
    private lateinit var apiMapper: ApiMapperService

    @Autowired
    private lateinit var originalDataService: OriginalDataService

    @Autowired
    private lateinit var manageDataService: ManageDataService


    @RequestMapping(value = ["/data"], produces = ["application/json"])
    fun data(): ApiData {
        val data = apiMapper.data()
        return ApiData(
                current = apiMapper.currentValuesMap(),
                series = data.order,
                data = data.dataList,
                minMax = data.minMax
        )
    }


    @RequestMapping(value = ["/original/{type}"], produces = ["application/csv"])
    fun originalData(@PathVariable("type") type: String): ResponseEntity<String> {
        val typeEnum = config.topicToSensor[type] ?: return ResponseEntity(HttpStatus.BAD_REQUEST)

        val result = originalDataService.createCsv(typeEnum)
        return ResponseEntity.ok(result)
    }


    @DeleteMapping(value = ["/manage/data"])
    fun deleteOldData(@RequestParam("keepHours") keepHours: Int): ResponseEntity<Void> {
        manageDataService.deleteOld(keepHours)
        return ResponseEntity.ok(null)
    }


}
