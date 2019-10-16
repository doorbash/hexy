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
public class Item extends Schema {

	public static final int TYPE_COIN = 1;
	public static final int TYPE_BOOST = 2;

	@SchemaField("0/float32")
	public float x = 0;

	@SchemaField("1/float32")
	public float y = 0;

	@SchemaField("2/uint8")
	public short type = 0;

	//	@SchemaField("3/uint8")
//	public short speed = 0;

	public Sprite sprite;
}

