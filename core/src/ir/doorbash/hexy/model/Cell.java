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
public class Cell extends Schema {
	@SchemaField("0/int16")	
	public short x = 0;

	@SchemaField("1/int16")	
	public short y = 0;

	@SchemaField("2/uint8")	
	public short color = 0;

	@SchemaField("3/string")	
	public String owner = "";
}

