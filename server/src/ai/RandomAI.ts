import { Player } from "../model/Player";
import { Room } from "colyseus";

const SKIP_THINKING = 20;

export class RandomAI {

    /********************************************** FIELDS **********************************************/

    skip: number = SKIP_THINKING;
    player: Player;

    /******************************************** CONSTRUCTOR ********************************************/

    constructor(room: Room, player: Player, mapSizeXPixel, mapSizeYPixel) {
        this.player = player;
    }

    /********************************************* FUNCTIONS *********************************************/

    getAngle() {
        if (this.skip < SKIP_THINKING) {
            this.skip++;
            return this.player.new_angle;
        }
        this.skip = 0;

        var angle = this.player.new_angle + (Math.random() * Math.PI / 10 - Math.PI / 20);
        if (angle < -Math.PI) angle += 2 * Math.PI;
        else if (angle > Math.PI) angle -= 2 * Math.PI;
        return angle;
    }
    
}