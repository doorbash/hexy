package io.github.doorbash.hexy.model

import com.badlogic.gdx.graphics.g2d.Sprite
import io.colyseus.annotations.SchemaField
import io.colyseus.default
import io.colyseus.serializer.schema.Schema

class Cell : Schema() {
    @SchemaField("0/int16")
    var x = Short.default

    @SchemaField("1/int16")
    var y = Short.default

    @SchemaField("2/uint32")
    var pid = Long.default

    var colorSprite: Sprite? = null
    var textureSprite: Sprite? = null
}