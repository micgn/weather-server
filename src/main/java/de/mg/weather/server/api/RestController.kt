package de.mg.weather.server.api


import de.mg.weather.server.model.SensorData
import org.springframework.beans.factory.annotation.Autowired

@org.springframework.web.bind.annotation.RestController
class RestController {

    @Autowired
    private lateinit var sensorData: SensorData

/*
    @RequestMapping(value = "/data", produces = ["application/json"])
    fun monthlySums(): ApiData {

        return ApiData()
    }
*/

}
