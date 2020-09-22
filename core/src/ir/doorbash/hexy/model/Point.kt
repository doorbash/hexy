package ir.doorbash.hexy.model

import io.colyseus.annotations.SchemaField
import io.colyseus.default
import io.colyseus.serializer.schema.Schema

class Point : Schema() {
    @SchemaField("0/float32")
    var x = Float.default

    @SchemaField("1/float32")
    var y = Float.default
}