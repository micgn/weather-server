package de.mg.weather.server.db

import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
class SensorValueId : Serializable {


    @Column(nullable = false)
    var type: SensorEnum? = null


    @Column(nullable = false)
    var time: Long? = null

}