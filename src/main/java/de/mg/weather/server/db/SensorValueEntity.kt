package de.mg.weather.server.db

import javax.persistence.Column
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table
class SensorValueEntity {

    @EmbeddedId
    var id: SensorValueIdEntity? = null


    @Column(nullable = false)
    var value: Float? = null

}
