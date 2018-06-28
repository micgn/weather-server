package de.mg.weather.server.api


import de.mg.weather.server.model.SensorEnum
import de.mg.weather.server.service.ApiMapperService
import de.mg.weather.server.service.OriginalDataService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import javax.websocket.server.PathParam


@org.springframework.web.bind.annotation.RestController
class RestController {

    @Autowired
    private lateinit var apiMapper: ApiMapperService

    @Autowired
    private lateinit var originalDataService: OriginalDataService


    @RequestMapping(value = "/data", produces = ["application/json"])
    fun data() = ApiData(
            current = apiMapper.currentValuesMap(),
            series = apiMapper.data().order,
            data = apiMapper.data().dataList
    )


    @RequestMapping(value = "/original/{type}", produces = ["application/csv"])
    fun originalData(@PathParam("type") type: String): ResponseEntity<String> {
        val typeEnum = try {
            SensorEnum.valueOf(type)
        } catch (e: IllegalArgumentException) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        val result = originalDataService.createCsv(typeEnum)
        return ResponseEntity.ok(result)
    }


}
