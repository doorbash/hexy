package io.github.doorbash.hexy.model

import io.colyseus.annotations.SchemaField
import io.colyseus.default
import io.colyseus.serializer.schema.Schema
import io.colyseus.serializer.schema.types.MapSchema

class MyState : Schema() {
    @SchemaField("0/map/ref", Player::class)
    var players = MapSchema(Player::class.java)

    @SchemaField("1/map/ref", Item::class)
    var items = MapSchema(Item::class.java)

    @SchemaField("2/boolean")
    var started = Boolean.default

    @SchemaField("3/boolean")
    var ended = Boolean.default

    @SchemaField("4/int64")
    var startTime = Long.default

    @SchemaField("5/int64")
    var endTime = Long.default
}