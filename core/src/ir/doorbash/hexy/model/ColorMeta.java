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
public class ColorMeta extends Schema {

    public static final int CHANGE_DIRECTION_UP = 1;
    public static final int CHANGE_DIRECTION_DOWN = -1;

    @SchemaField("0/uint8")
    public short color = 0;

    @SchemaField("1/uint16")
    public int numCells = 0;

    @SchemaField("2/uint8")
    public short position = 0;

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ColorMeta && ((ColorMeta) obj).color == color;
    }

    @Override
    public String toString() {
        return "{" + "color=" + color + ", " + "numcells=" + numCells + ", " + "position=" + position + ", " + "_position=" + _position + "}";
    }

    public Sprite progressBar;
    public int _position; // positioni ke alan dare draw mishe
    public boolean positionIsChanging = false; // aya jash too list dare avaz mishe ya na
    // (dar halate harekate be samte ye position e dge)
    public float _percentage; // braye lerp estefade mishe (qashangi)
    public int changeDir; // dare mire bala ya paeen vaghti positionIsChanging = true e
}

