package ir.doorbash.hexy.model;//
// THIS FILE HAS BEEN GENERATED AUTOMATICALLY
// DO NOT CHANGE IT MANUALLY UNLESS YOU KNOW WHAT YOU'RE DOING
// 
// GENERATED USING @colyseus/schema 0.4.41
// 


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;

import java.util.LinkedList;

import io.colyseus.serializer.schema.Schema;
import io.colyseus.serializer.schema.annotations.SchemaClass;
import io.colyseus.serializer.schema.annotations.SchemaField;
import ir.doorbash.hexy.TrailGraphic;
import ir.doorbash.hexy.util.PathCellUpdate;

@SchemaClass
public class Player extends Schema {

	public static final boolean CHANGE_DIRECTION_DOWN = false;
	public static final boolean CHANGE_DIRECTION_UP = true;

	@SchemaField("0/uint8")	
	public short pid = 0;

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
	public ArraySchema<Cell> path_cells = new ArraySchema<>(Cell.class);

	@SchemaField("8/uint8")
	public short status = 0;

	@SchemaField("9/int64")
	public long rspwnTime = 0;

	@SchemaField("10/string")
	public String name = "";

	@SchemaField("11/uint16")
	public int speed = 0;

	@SchemaField("12/uint16")
	public int kills = 0;

	@SchemaField("13/uint16")
	public int numCells = 0;

	@SchemaField("14/string")
	public String fill = "";

	@SchemaField("15/string")
	public String stroke = "";

//    @SchemaField("12/boolean")
//    public boolean home = false;

	public Sprite _stroke;
	public Sprite _fill;
	public Sprite indic;
	public Sprite bcGhost;
	public Sprite progressBar;
	public GlyphLayout text;
//	public final HashMap<Integer, Cell> pathCells = new HashMap<>();
	public TrailGraphic trailGraphic;
	public final LinkedList<PathCellUpdate> pathCellUpdates = new LinkedList<>();
	public String _name;
	public float _angle;
	public int _position;
	public float _percentage;
	public boolean positionIsChanging;
	public int position;
	public boolean changeDir;
	public Color fillColor;
	public Color strokeColor;
	public Color progressColor;
	public Color pathCellColor;

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Player && ((Player) obj).clientId.equals(clientId);
	}
}

