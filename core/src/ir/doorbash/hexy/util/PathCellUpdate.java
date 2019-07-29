package ir.doorbash.hexy.util;

import ir.doorbash.hexy.model.Cell;

/**
 * Created by Milad Doorbash on 7/29/2019.
 */
public class PathCellUpdate {
    public Cell cell;
    public long time;
    public int key;

    public PathCellUpdate(Cell cell, int key, long time) {
        this.cell = cell;
        this.time = time;
        this.key = key;
    }
}
