package de.mg.weather.server.api


import de.mg.weather.server.service.ApiMapperService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping

@org.springframework.web.bind.annotation.RestController
class RestController {

    @Autowired
    lateinit var apiMapper: ApiMapperService


    @RequestMapping(value = "/data", produces = ["application/json"])
    fun data() =

            ApiData(
                    current = apiMapper.currentValuesMap(),
                    series = apiMapper.data().order,
                    data = apiMapper.data().dataList
            )

}
