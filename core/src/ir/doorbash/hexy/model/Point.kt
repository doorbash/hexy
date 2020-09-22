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
public class Point extends Schema {
	@SchemaField("0/float32")
	public float x = 0;

	@SchemaField("1/float32")
	public float y = 0;

//	@SchemaField("2/uint8")
//	public short color = 0;
}

