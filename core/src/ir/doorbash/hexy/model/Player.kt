package ir.doorbash.hexy.model

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.Sprite
import io.colyseus.annotations.SchemaField
import io.colyseus.default
import io.colyseus.serializer.schema.Schema
import io.colyseus.serializer.schema.types.ArraySchema
import io.colyseus.serializer.schema.types.MapSchema
import ir.doorbash.hexy.TrailGraphic

class Player : Schema() {
    @SchemaField("0/uint32")
    var pid = Long.default

    @SchemaField("1/float32")
    var x = Float.default

    @SchemaField("2/float32")
    var y = Float.default

    @SchemaField("3/float32")
    var angle = Float.default

    @SchemaField("4/float32")
    var new_angle = Float.default

    @SchemaField("5/string")
    var clientId = String.default

    @SchemaField("6/array/ref", Point::class)
    var path = ArraySchema(Point::class.java)

    @SchemaField("7/map/ref", Cell::class)
    var cells = MapSchema(Cell::class.java)

    @SchemaField("8/array/ref", Cell::class)
    var path_cells = ArraySchema(Cell::class.java)

    @SchemaField("9/uint8")
    var status = Short.default

    @SchemaField("10/int64")
    var rspwnTime = Long.default

    @SchemaField("11/string")
    var name = String.default

    @SchemaField("12/uint16")
    var speed = Int.default

    @SchemaField("13/uint16")
    var kills = Int.default

    @SchemaField("14/uint16")
    var numCells = Int.default

    @SchemaField("15/string")
    var fill = String.default

    @SchemaField("16/string")
    var stroke = String.default

    @SchemaField("17/uint16")
    var coins = Int.default

    var _stroke: Sprite? = null
    var _fill: Sprite? = null
    var indic: Sprite? = null
    var bcGhost: Sprite? = null
    var progressBar: Sprite? = null
    var text: GlyphLayout? = null
    var trailGraphic: TrailGraphic? = null
    var _name: String? = null
    var _angle = 0f
    var _position = 0
    var _percentage = 0f
    var positionIsChanging = false
    var position = 0
    var changeDir = false
    var fillColor: Color? = null
    var strokeColor: Color? = null
    var progressColor: Color? = null
    var pathCellColor: Color? = null
    var fillIsTexture = false

    override fun equals(other: Any?): Boolean {
        return other is Player && other.clientId == clientId
    }

    companion object {
        const val CHANGE_DIRECTION_DOWN = false
        const val CHANGE_DIRECTION_UP = true
    }
}