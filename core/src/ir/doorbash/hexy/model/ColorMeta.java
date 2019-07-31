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
import ir.doorbash.hexy.ProgressBar;

@SchemaClass
public class ColorMeta extends Schema {
	@SchemaField("0/uint8")	
	public short color = 0;

	@SchemaField("1/uint16")	
	public int numCells = 0;

	@SchemaField("2/uint8")	
	public short position = 0;

	public Sprite progressBar;
}

