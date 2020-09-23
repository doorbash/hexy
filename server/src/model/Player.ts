import { Schema, type, ArraySchema, MapSchema } from "@colyseus/schema"
import { Client } from "colyseus"
import { Point } from "./Point";
import { Cell } from "./Cell";

export class Player extends Schema {
    @type("uint32")
    pid: number = 0;

    @type("float32")
    x: number = 0;

    @type("float32")
    y: number = 0;

    @type("float32")
    angle: number = 0;

    @type("float32")
    new_angle: number = Math.random() * Math.PI * 2 - Math.PI;

    @type("string")
    clientId: string;

    @type([Point])
    path = new ArraySchema<Point>();

    @type({map: Cell})
    cells = new MapSchema<Cell>();

    @type([Cell])
    path_cells = new ArraySchema<Cell>();

    @type("uint8")
    status: number = 0;

    @type("int64")
    rspwnTime: number;

    @type("string")
    name: string;

    @type("uint16")
    speed: number = 0;

    @type("uint16")
    kills:number = 0;

    @type("uint16")
    numCells:number = 0;

    @type("string")
    fill:string = "#000000";

    @type("string")
    stroke:string = "#000000";
    
    @type("uint16")
    coins:number = 0;

    home: boolean = true;
    ai: any;
    xi: number;
    yi: number;
    outside: boolean = false;
    client: Client;
    online: boolean;
    leaveTime: number;
    lastPoint:Point;
    lastPingTime:number;
    minX:number = 1000000;
    minY:number = 1000000;
    maxX:number = -1000000;
    maxY:number = -1000000;
    // time:number = 0;
    readyToPlay:boolean = false;
    dead:boolean = false;
    uid:string;
    boost_time:number;
    boost:boolean = false;
}