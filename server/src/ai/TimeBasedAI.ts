import { Player } from "../model/Player";
// import { Battle } from "../rooms/Battle";
import { FreeForAll } from "../rooms/FreeForAll";

/********************************************* CONSTANTS *********************************************/

const BOT_TURN_ANGLE = 20;
const SKIP_THINKING = 10;
const TIME_OUTSIDE = 500; // milliseconds

/*********************************************** CLASS ***********************************************/

export class TimeBasedAI {

    /******************************************** FIELDS ********************************************/

    d_angle: number = 0;
    room: FreeForAll;
    player: Player;
    inside: boolean = true;
    skip: number = Math.floor(Math.random() * SKIP_THINKING);
    mapSizeXPixel: number;
    mapSizeYPixel: number;
    lastTime: number = 0;
    playerIsHome: boolean;

    /******************************************** CONSTRUCTOR ********************************************/

    constructor(room: FreeForAll, player: Player, mapSizeXPixel, mapSizeYPixel) {
        this.d_angle = 0; // Math.floor(Math.random() * 6 - 3) / 180 * Math.PI; // +- deg
        this.room = room;
        this.player = player;
        this.mapSizeXPixel = mapSizeXPixel;
        this.mapSizeYPixel = mapSizeYPixel;
    }

    /********************************************* FUNCTIONS *********************************************/

    getAngle() {
        if (this.player.home != this.playerIsHome) {
            if (!this.player.home) this.lastTime = Date.now(); // dare mire biroon
            this.playerIsHome = this.player.home;
        }

        if (this.skip < SKIP_THINKING) {
            this.skip++;

            return this.player.new_angle;
        }
        this.skip = 0;

        if (this.player.numCells == 0) return this.player.new_angle;

        if (this.player.x > this.mapSizeXPixel) {
            if (this.inside) {
                if (this.player.angle >= 0 && this.player.angle <= Math.PI) {
                    this.d_angle = BOT_TURN_ANGLE * Math.PI / 180
                } else {
                    this.d_angle = -BOT_TURN_ANGLE * Math.PI / 180
                }
            }
            this.inside = false;
        } else if (this.player.x < -this.mapSizeXPixel) {
            if (this.inside) {
                if (this.player.angle >= 0 && this.player.angle <= Math.PI) {
                    this.d_angle = -BOT_TURN_ANGLE * Math.PI / 180
                } else {
                    this.d_angle = BOT_TURN_ANGLE * Math.PI / 180
                }

            }
            this.inside = false;
        } else if (this.player.y > this.mapSizeYPixel) {
            if (this.inside) {
                if (this.player.angle >= -Math.PI / 2 && this.player.angle <= Math.PI / 2) {
                    this.d_angle = -BOT_TURN_ANGLE * Math.PI / 180
                } else {
                    this.d_angle = BOT_TURN_ANGLE * Math.PI / 180
                }
            }
            this.inside = false;
        } else if (this.player.y < -this.mapSizeYPixel) {
            if (this.inside) {
                if (this.player.angle >= -Math.PI / 2 && this.player.angle <= Math.PI / 2) {
                    this.d_angle = BOT_TURN_ANGLE * Math.PI / 180
                } else {
                    this.d_angle = -BOT_TURN_ANGLE * Math.PI / 180
                }
            }
            this.inside = false;
        } else {
            if (!this.inside) {
                this.d_angle = 0; // yani hamin farmoon bro
            }
            this.inside = true;

            if (!this.player.home) {
                if (this.lastTime > 0 && this.lastTime < Date.now() - TIME_OUTSIDE) {
                    // return to home
                    this.lastTime = Date.now();
                    this.d_angle = 0;
                    var angle = this.getAngleToHome3();
                    if (angle < -Math.PI) angle += 2 * Math.PI;
                    else if (angle > Math.PI) angle -= 2 * Math.PI;
                    return angle;
                }
            }
        }

        var angle = this.player.new_angle + this.d_angle;
        if (angle < -Math.PI) angle += 2 * Math.PI;
        else if (angle > Math.PI) angle -= 2 * Math.PI;
        return angle;
    }

    getAngleToHome() {
        console.log("getAngleToHome() " + Date.now())
        var min_distance = 1000000;
        var _cell;
        for (var i = 0; i < this.room.cellIndex; i++) {
            var cell = this.room.state.cells[i];
            if (cell == undefined) continue;
            if (cell.pid != this.player.pid) continue;
            var distance = this.getP2DistanceFromTwoPoints({ x: this.player.xi, y: this.player.yi }, cell);
            if (distance < min_distance) {
                _cell = cell;
                min_distance = distance;
            }
            break;
        }
        var pos = this.room.getHexPosition(_cell.x, _cell.y);
        return _cell ? Math.atan2(pos.y - this.player.y, pos.x - this.player.x) : 0;
    }

    getAngleToHome2() {
        console.log("getAngleToHome() " + Date.now())
        for (var i = 0; i < 360; i += 5) {
            let rad = i * Math.PI / 180;
            if (this.simulate(this.player, rad, 500)) {
                console.log("answer = " + i)
                return rad;
            }
        }
        return this.player.new_angle;
    }

    getAngleToHome3() {
        // console.log("getAngleToHome() " + Date.now())
        // var min_distance = 1000000;
        var _cell;
        for (var xi = this.player.xi - 40; xi <= this.player.xi + 40; xi++) {
            if(!this.room.cells[xi]) continue;
            for (var yi = this.player.yi - 40; yi <= this.player.yi + 40; yi++) {
                var cell = this.room.cells[xi][yi];
                if(!cell) continue;
                if (cell.pid != this.player.pid) continue;
                _cell = cell;
                // var distance = this.getP2DistanceFromTwoPoints({ x: this.player.xi, y: this.player.yi }, cell);
                // if (distance < min_distance) {
                   
                //     min_distance = distance;
                // }
                break;
            }
        }

        // if(!_cell)  {
        //     console.log("_cell is undefined let's do it the hard way...");
        //     let cellKeys = Object.keys(this.room.state.cells);
        //     for(let i = 0;i < cellKeys.length; i++) {
        //         let cell = this.room.state.cells[cellKeys[i]];
        //         if(!cell) continue;
        //         if (cell.pid != this.player.pid) continue;
        //         _cell = cell;
        //         break;
        //     }
        // }

        if(!_cell) return 0;

        var pos = this.room.getHexPosition(_cell.x, _cell.y);
        return Math.atan2(pos.y - this.player.y, pos.x - this.player.x);
    }

    simulate(player, angle, range): boolean {
        var x = player.x;
        var y = player.y;
        var delta = 0;
        var pace = player.speed * 0.05;
        while (delta < range) {
            x += Math.cos(angle) * pace;
            y += Math.sin(angle) * pace;
            let pos = this.room.positionToHex(x, y);
            let xi = pos.x;
            let yi = pos.y;
            if (this.room.cells[xi] && this.room.cells[xi][yi] && this.room.cells[xi][yi].pid == player.pid)
                return true;
            delta += pace;
        }
        return false;
    }


    getP2DistanceFromTwoPoints(c1, c2) {
        return (c1.x - c2.x) * (c1.x - c2.x) + (c1.y - c2.y) * (c1.y - c2.y);
    }
}