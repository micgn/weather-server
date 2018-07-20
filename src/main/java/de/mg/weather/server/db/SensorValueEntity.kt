package de.mg.weather.server.db

import javax.persistence.Column
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table
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
