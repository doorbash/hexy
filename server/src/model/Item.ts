import { Schema, type } from "@colyseus/schema"

export class Item extends Schema {

    static TYPE_COIN = 1;
    static TYPE_BOOST = 2;

    @type("float32")
    x: number = 0;

    @type("float32")
    y: number = 0;

    @type("uint8")
    type: number = 0; // coin = 1, boost = 2e;

    //@type("uint8")
    speed: number = 50;
    angle: number = Math.random() * 2 * Math.PI - Math.PI;
}