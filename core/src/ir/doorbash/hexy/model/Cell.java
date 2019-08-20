package ir.doorbash.hexy.model;//
// THIS FILE HAS BEEN GENERATED AUTOMATICALLY
// DO NOT CHANGE IT MANUALLY UNLESS YOU KNOW WHAT YOU'RE DOING
// 
// GENERATED USING @colyseus/schema 0.4.41
// 


import com.badlogic.gdx.graphics.g2d.Sprite;

import io.colyseus.serializer.schema.Schema;
import io.colyseus.serializer.schema.annotations.SchemaClass;
import io.colyseus.serializer.schema.annotations.SchemaField;

@SchemaClass
public class Cell extends Schema {

	public static final int CELL_TYPE_COLOR = 0;
	public static final int CELL_TYPE_TEXTURE = 1;

	@SchemaField("0/int16")
	public short x = 0;

	@SchemaField("1/int16")	
	public short y = 0;

	@SchemaField("2/uint32")
	public long pid = 0;

//	@SchemaField("3/string")
//	public String owner = "";

	public Sprite sprite;
	public int type;
}

