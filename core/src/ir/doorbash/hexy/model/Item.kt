package ir.doorbash.hexy.model

import com.badlogic.gdx.graphics.g2d.Sprite
import io.colyseus.annotations.SchemaField
import io.colyseus.default
import io.colyseus.serializer.schema.Schema

class Item : Schema() {
    @SchemaField("0/float32")
    var x = Float.default

    @SchemaField("1/float32")
    var y = Float.default

    @SchemaField("2/uint8")
    var type = Short.default


    var sprite: Sprite? = null

    companion object {
        const val TYPE_COIN = 1
        const val TYPE_BOOST = 2
    }
}