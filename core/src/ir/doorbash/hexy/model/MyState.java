package ir.doorbash.hexy.model;//
// THIS FILE HAS BEEN GENERATED AUTOMATICALLY
// DO NOT CHANGE IT MANUALLY UNLESS YOU KNOW WHAT YOU'RE DOING
// 
// GENERATED USING @colyseus/schema 0.4.41
// 


import io.colyseus.serializer.schema.Schema;
import io.colyseus.serializer.schema.annotations.SchemaClass;
import io.colyseus.serializer.schema.annotations.SchemaField;

@SchemaClass
public class MyState extends Schema {
    @SchemaField("0/map/ref")
    public MapSchema<Player> players = new MapSchema<>(Player.class);

    @SchemaField("1/map/ref")
    public MapSchema<Item> items = new MapSchema<>(Item.class);

    @SchemaField("2/boolean")
    public boolean started = false;

    @SchemaField("3/boolean")
    public boolean ended = false;

    @SchemaField("4/int64")
    public long startTime = 0;

    @SchemaField("5/int64")
    public long endTime = 0;

    @Override
    public MyState _clone() {
        MyState copy = new MyState();
        copy.players = (MapSchema<Player>) players._clone();
        copy.items = (MapSchema<Item>) items._clone();
        copy.started = started;
        copy.ended = ended;
        copy.startTime = startTime;
        copy.endTime = endTime;
        return copy;
    }
}

