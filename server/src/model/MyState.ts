import { Schema, type, ArraySchema, MapSchema } from "@colyseus/schema"
import { Cell } from "./Cell";
import { Player } from "./Player";
import { Item } from "./Item";

export class MyState extends Schema {
    @type({ map: Player })
    players = new MapSchema<Player>();

    @type({ map: Item})
    items = new MapSchema<Item>();

    @type("boolean")
    started: boolean = false;

    @type("boolean")
    ended: boolean = false;

    @type("int64")
    startTime: number = 0;

    @type("int64")
    endTime: number = 0;
}