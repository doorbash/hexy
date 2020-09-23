import { Schema, type } from "@colyseus/schema"

export class Point extends Schema {
    @type("float32")
    x: number = 0;

    @type("float32")
    y: number = 0;
}