package de.mg.weather.server.db

import javax.persistence.*

@Entity
@Table(indexes = [Index(columnList = "time")])
class SensorValueEntity {

    @EmbeddedId
    private var id: SensorValueIdEntity? = null

    fun getId() = id!!
    fun setId(id: SensorValueIdEntity) {
        this.id = id
    }


    @Column(nullable = false)
    private var value: Float? = null

    fun getValue() = value!!
    fun setValue(value: Float) {
        this.value = value
    }

}
