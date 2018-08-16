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

    // equals and hashcode need to be overwritten for composite keys:

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SensorValueIdEntity

        if (type != other.type) return false
        if (time != other.time) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type?.hashCode() ?: 0
        result = 31 * result + (time?.hashCode() ?: 0)
        return result
    }


}