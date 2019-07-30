package ir.doorbash.hexy.model;//
// THIS FILE HAS BEEN GENERATED AUTOMATICALLY
// DO NOT CHANGE IT MANUALLY UNLESS YOU KNOW WHAT YOU'RE DOING
// 
// GENERATED USING @colyseus/schema 0.4.41
// 


import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import io.colyseus.serializer.schema.Schema;
import io.colyseus.serializer.schema.annotations.SchemaClass;
import io.colyseus.serializer.schema.annotations.SchemaField;
import ir.doorbash.hexy.TrailGraphic;
import ir.doorbash.hexy.util.PathCellUpdate;

@SchemaClass
public class Player extends Schema {
	@SchemaField("0/uint8")	
	public short color = 0;

	@SchemaField("1/float32")
	public float x = 0;

	@SchemaField("2/float32")
	public float y = 0;

	@SchemaField("3/float32")
	public float angle = 0;

	@SchemaField("4/float32")
	public float new_angle = 0;

	@SchemaField("5/string")	
	public String clientId = "";

	@SchemaField("6/array/ref")	
	public ArraySchema<Point> path = new ArraySchema<>(Point.class);

	@SchemaField("7/array/ref")	
	public ArraySchema<Cell> cells = new ArraySchema<>(Cell.class);

	@SchemaField("8/uint8")	
	public short status = 0;

	@SchemaField("9/int64")	
	public long rspwnTime = 0;

	@SchemaField("10/string")	
	public String name = "";

	@SchemaField("11/uint16")
	public int speed = 0;

//    @SchemaField("12/boolean")
//    public boolean home = false;

	public Sprite bc;
	public Sprite c;
	public Sprite indic;
	public Sprite bcGhost;
	public GlyphLayout text;
	public final HashMap<Integer, Cell> pathCells = new HashMap<>();
	public TrailGraphic trailGraphic;
	public final LinkedList<PathCellUpdate> pathCellUpdates = new LinkedList<>();

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Player && ((Player) obj).clientId.equals(clientId);
	}
}

