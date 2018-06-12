package de.mg.weather.server.db

import javax.persistence.Column
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table
class SensorValue {

    @EmbeddedId
    var id: SensorValueId? = null


    @Column(nullable = false)
    var value: Float? = null

}
