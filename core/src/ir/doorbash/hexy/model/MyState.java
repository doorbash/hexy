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
	public MapSchema<Cell> cells = new MapSchema<>(Cell.class);

	@SchemaField("1/map/ref")	
	public MapSchema<Player> players = new MapSchema<>(Player.class);

	@SchemaField("2/map/ref")	
	public MapSchema<ColorMeta> colorMeta = new MapSchema<>(ColorMeta.class);

	@SchemaField("3/boolean")	
	public boolean started = false;

	@SchemaField("4/boolean")	
	public boolean ended = false;

	@SchemaField("5/int64")	
	public long startTime = 0;

	@SchemaField("6/int64")	
	public long endTime = 0;
}

