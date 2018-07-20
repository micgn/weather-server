package de.mg.weather.server.db

import de.mg.weather.server.model.SensorEnum
import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
class SensorValueIdEntity : Serializable {


    @Column(nullable = false)
    private var type: SensorEnum? = null

    fun getType() = type!!
    fun setType(type: SensorEnum) {
        this.type = type
    }


    @Column(nullable = false)
    private var time: Long? = null

    fun getTime() = time!!
    fun setTime(time: Long) {
        this.time = time
    }

}