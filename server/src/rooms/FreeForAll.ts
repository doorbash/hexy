import { Room, Client } from "colyseus"
import { MyState } from "../model/MyState";
import { Player } from "../model/Player";
import { Cell } from "../model/Cell";
import { Point } from "../model/Point";
import { generateUsername, generateId } from "../util/TextUtil";
import { maximalRectangle } from "../util/MaximalRectangle";
import { angleDistance } from "../util/MathUtil"
import { TimeBasedAI } from "../ai/TimeBasedAI";
import { getRandomFillStrokePair, getFillStrokePair } from "../util/ColorUtil";
import { isTextureValid, getRandomTexture } from "../util/TextureUtil";
import { IncomingMessage } from "http"
import { get, post, patch } from "httpie";
import { CONSTANTS } from ".."
import { Item } from "../model/Item";

const PLAYER_STATUS_NORMAL = 0;

const WORLD_UPDATE_INTERVAL = 30;
const SKIP_PLAYER_PATH = 2;
const MIN_WIDTH = 3;
const MIN_HEIGHT = 4;
const PLAYER_CLIENT_CLOSE_TIMEOUT = 5; // seconds
const PLAYER_RECONNECT_WAIT = 30; // seconds
const GRID_WIDTH = 44;
const GRID_HEIGHT = 38;
const MAP_SIZE = 50;
const EXTENDED_CELLS = 4;
const MAP_SIZE_X_PIXEL = MAP_SIZE * GRID_WIDTH;
const MAP_SIZE_X_EXT_PIXEL = (MAP_SIZE + EXTENDED_CELLS) * GRID_WIDTH;
const MAP_SIZE_Y_PIXEL = MAP_SIZE * GRID_HEIGHT;
const MAP_SIZE_Y_EXT_PIXEL = (MAP_SIZE + EXTENDED_CELLS) * GRID_HEIGHT;
const TOTAL_CELLS = (2 * MAP_SIZE + 1) * (2 * MAP_SIZE + 1);
const JOIN_FREE_CELLS_PERCENT = 0.50; // percent
const ADD_BOT_INTERVAL = 1; // seconds
const TOTAL_BOTS = 10;
const PLAYER_ROTATE_SPEED = 2;
const PLAYER_NO_UPDATE_TIME = 2000; // seconds
const BOTS_CAN_HAVE_TEXTURE: boolean = true;
const NUM_TOTAL_COINS = 50;
const NUM_TOTAL_BOOSTS = 50;
const ADD_ITEM_INTERVAL = 1; // seconds
const COIN_ITEM_ADD_VALUE = 10;
const ITEM_EAT_DISTANCE = 35;
const BOOST_TIME = 10; // seconds
const PLAYER_SPEED_NORMAL = 180;
const PLAYER_SPEED_BOOST = 220;

export class FreeForAll extends Room {

    /****************************************** FIELDS ***********************************************/

    maxClients = 20;
    autoDispose = false;

    players = {};
    skipPlayerPath = SKIP_PLAYER_PATH
    cells = {};
    pathCells = {};
    fill = [];
    fillNumber = 1;
    fillOutsideAreas = [];
    fillQueue = [];
    map = [];
    cellIndex = 0;
    numFreeCells: number = TOTAL_CELLS;
    numHumanPlayers: number = 0;
    numBots: number = 0;
    lastJoinTime: number = 0;
    currentPid = 0;
    currentItemKey = 0;
    lastItemCheckTime = 0;

    /***************************************** FUNCTIONS *********************************************/

    onCreate?(options: any): void {
        console.log("Room created!", options);
        this.setState(new MyState());
        for (let i = 0; i < 2 * MAP_SIZE + 1; i++) {
            this.map[i] = [];
            for (let j = 0; j < 2 * MAP_SIZE + 1; j++) this.map[i][j] = 1;
        }
        this.state.startTime = Date.now();
        this.state.endTime = 0;
        this.state.started = true;
        this.setSimulationInterval(dt => this.updateWorld(dt / 1000), WORLD_UPDATE_INTERVAL);
        this.clock.setInterval(() => {
            if (!this.locked && this.numBots < TOTAL_BOTS && this.lastJoinTime < Date.now() - ADD_BOT_INTERVAL * 1000 && this.numFreeCells >= JOIN_FREE_CELLS_PERCENT * TOTAL_CELLS) {
                this.addBot();
            }
        }, 2 * 1000);

        this.onMessage("angle", (client, value) => {
            var player = this.state.players.get(client.id);
            if(player == undefined) return
            player.new_angle = value * Math.PI / 180;
        });

        this.onMessage("ping", (client, value) => {
            var player = this.state.players.get(client.id);
            if(player == undefined) return
            player.lastPingTime = Date.now();
            client.send("ping", { t: value, st: player.lastPingTime });
        });

        this.onMessage("ready", (client, value) => {
            var player = this.state.players.get(client.id);
            if(player == undefined) return
            player.readyToPlay = true;
        });
    }

    onAuth(client: Client, options: any, request?: IncomingMessage): any | Promise<any> {
        if (options.id) return options.id
        else return generateId(20)
        // if (options.id) {
        //     await patch(CONSTANTS.api + "/user/" + options.id, { headers: { 'Accept': 'application/json' }, body: { name: options.name ? options.name : "guest" } });
        //     return options.id;
        // } else {
        //     return (await post(CONSTANTS.api + "/user/", { headers: { 'Accept': 'application/json' }, body: { name: options.name ? options.name : "guest" } })).data.id;
        // }
    }

    onJoin?(client: Client, options?: any, auth?: any): void | Promise<any> {
        console.log('onJoin(', client.id, ')');
        console.log(";;;;;;;;;;;;;;;;;;;;;;;;");
        console.log("options.id = " + options.id + ", auth = " + auth);
        console.log(";;;;;;;;;;;;;;;;;;;;;;;;");


        console.log(">>>>>>>>> client.id = " + client.id)

        if (!this.state.players.has(client.id)) {
            console.log("OK. new player joined!");
            let pid = ++this.currentPid;
            let player = new Player();
            player.pid = pid;
            player.clientId = client.id;
            let color = getFillStrokePair(pid);
            if (options.fill && ((options.fill.startsWith("#") && options.fill.length == 9) || isTextureValid(options.fill))) {
                player.fill = options.fill;
            } else {
                player.fill = color.fill;
            }
            if (options.stroke && options.stroke.startsWith("#") && options.stroke.length == 9) {
                player.stroke = options.stroke;
            } else {
                player.stroke = color.stroke;
            }
            this.state.players.set(client.id, player);
            this.players[pid] = player;
            let recInfo = maximalRectangle(this.map);
            // console.log(recInfo);
            let x = Math.floor((recInfo.x1 + recInfo.x2 - MIN_WIDTH) / 2) - MAP_SIZE;
            let y = Math.floor((recInfo.y1 + recInfo.y2 - MIN_HEIGHT) / 2) - MAP_SIZE;
            let pos = this.add_cells(x, y, pid);
            pos = this.getHexPosition(pos.x + 1, pos.y + 1);
            player.x = pos.x;
            player.y = pos.y;
            player.new_angle = player.angle = Math.random() * 2 * Math.PI;
            this.lastJoinTime = Date.now();
        }
        var player = this.state.players.get(client.id)
        player.name = options.name ? options.name : "guest";
        player.online = true;
        player.lastPingTime = Date.now();
        player.client = client;
        player.uid = auth;

        console.log(
            "client joined : \n" +
            "clientId= " + client.id + "\n" +
            "player.name= " + player.name + "\n" +
            "player.pid= " + player.pid + "\n"
        );

        client.send("welcome", { time: Date.now(), id: auth });
    }

    async onLeave?(client: Client, consented?: boolean): Promise<any> {
        console.log("onLeave(" + client.id + ", " + consented + ")");
        if (!this.state.players.has(client.id)) return;
        var player = this.state.players.get(client.id)
        player.online = false;
        player.leaveTime = Date.now();
        try {
            if (consented) {
                throw new Error("consented leave");
            }
            await this.allowReconnection(client, PLAYER_RECONNECT_WAIT);
        } catch (e) {
            // if(e) console.log(e);
            let player = this.state.players.get(client.id);
            if (player && !player.online && !player.dead) {
                console.log("RIP " + player.name + ", clientId= " + player.clientId + ", pid = " + player.pid);
                console.log("poor " + player.name + " is dead because they left or disconnected and haven't come back after " + PLAYER_RECONNECT_WAIT + " seconds :/");
                this.removePlayer(player, false);
            }
        }
    }

    onDispose() {
        console.log("Dispose Room");
    }

    add_cells(x, y, pid) {
        console.log("add_cells(x: " + x + ",y: " + y + ",pid: " + pid + ")");

        if (y % 2 == 0) y++;

        this.add_cell(x, y, pid);
        this.add_cell(x + 1, y, pid);

        this.add_cell(x, y + 1, pid);
        this.add_cell(x + 1, y + 1, pid);
        this.add_cell(x + 2, y + 1, pid);

        this.add_cell(x, y + 2, pid);
        this.add_cell(x + 1, y + 2, pid);

        return { x: x, y: y };
    }

    add_cell(x, y, pid) {
        if (x < -MAP_SIZE) return;
        if (x > MAP_SIZE) return;
        if (y < -MAP_SIZE) return;
        if (y > MAP_SIZE) return;

        let cell: Cell = new Cell();
        cell.x = x;
        cell.y = y;
        cell.pid = pid;
        if (!this.cells[cell.x]) this.cells[cell.x] = {};
        this.cells[cell.x][cell.y] = cell;
        // console.log("x = " + x + ", y = " + y);
        this.map[cell.x + MAP_SIZE][cell.y + MAP_SIZE] = 0;
        this.players[cell.pid].cells.set('k' + x * 10000 + y, cell);
        this.players[cell.pid].numCells++;
        // console.log("adding cell (" + x + "," + y + ") for pid=" + pid);
    }

    updateWorld(dt: number) {
        this.skipPlayerPath--;
        this.numHumanPlayers = 0;
        this.numBots = 0;
        let currentTime = Date.now();

        let list = [];
        // for (let i = 1; i <= this.maxClients; i++) {
        //     let player = this.players[i];
        //     if (!player) continue;
        //     list.push(player);
        // }
        Object.keys(this.players).forEach(pid => {
            let player: Player = this.players[pid];
            list.push(player);
        })
        list.sort((a, b): number => {
            return b.numCells - a.numCells;
        });
        list.forEach(player => {

            if (!player || player.dead) return;

            if (!player.ai) {
                this.numHumanPlayers++;
                // player.time += dt;
                // if (player.time < PLAYER_NO_UPDATE_TIME) return;
                if (!player.readyToPlay) return;
            }
            else {
                this.numBots++;
            }

            if (!player.boost) player.speed = PLAYER_SPEED_NORMAL;

            // check for player inactivity
            if (player.online === true && player.lastPingTime < (currentTime - 15000)) {
                this.removePlayer(player, false);
                return;
            }

            // let d = player.new_angle - player.angle;
            // let x = Math.abs(d);
            // if (x > Math.PI) d = Math.sign(d) * (2 * Math.PI - x) * -1;
            // player.angle = player.angle + d * 0.1;

            // if (player.angle < -Math.PI) player.angle += 2 * Math.PI
            // else if (player.angle > Math.PI) player.angle -= 2 * Math.PI

            player.angle += angleDistance(player.angle, player.new_angle) * PLAYER_ROTATE_SPEED * dt;

            let newX = player.x + Math.cos(player.angle) * player.speed * dt;
            let newY = player.y + Math.sin(player.angle) * player.speed * dt;

            if (newX <= MAP_SIZE_X_EXT_PIXEL && newX >= -MAP_SIZE_X_EXT_PIXEL) player.x = newX;
            if (newY <= MAP_SIZE_Y_EXT_PIXEL && newY >= -MAP_SIZE_Y_EXT_PIXEL) player.y = newY;

            player.yi = Math.floor((player.y + GRID_HEIGHT / 2) / GRID_HEIGHT);
            player.xi = player.yi % 2 == 0 ? Math.floor((player.x + GRID_WIDTH / 2) / GRID_WIDTH) : Math.floor(player.x / GRID_WIDTH);

            player.outside = false;
            if (player.xi < -MAP_SIZE) { player.xi = -MAP_SIZE; player.outside = true; }
            else if (player.xi > MAP_SIZE) { player.xi = MAP_SIZE; player.outside = true; }
            if (player.yi < -MAP_SIZE) { player.yi = -MAP_SIZE; player.outside = true; }
            else if (player.yi > MAP_SIZE) { player.yi = MAP_SIZE; player.outside = true; }

            /*if(!player.ai)*/ // console.log(player.clientId + " - " + player.name + " xi,yi = " + player.xi + ", " + player.yi + " outside = " + player.outside);

            if (player.xi < player.minX) player.minX = player.xi;
            else if (player.xi > player.maxX) player.maxX = player.xi;
            if (player.yi < player.minY) player.minY = player.yi;
            else if (player.yi > player.maxY) player.maxY = player.yi;

            if (this.state.started && !this.state.ended)
                this.processPlayerPosition(player, { x: player.xi, y: player.yi });

            this.processPlayerAi(player);

            this.checkIfPlayerEatItem(player);

            // this.validatePathCells();

            if (player.boost) {
                if (currentTime - player.boost_time >= BOOST_TIME * 1000) {
                    player.boost = false;
                    player.speed = PLAYER_SPEED_NORMAL;
                }
            }
        });

        if (this.skipPlayerPath <= 0) {
            this.skipPlayerPath = SKIP_PLAYER_PATH
        }

        this.state.items.forEach((item, key) => {
            item.x += Math.cos(item.angle) * item.speed * dt;
            item.y += Math.sin(item.angle) * item.speed * dt;
        });

        this.updateNumFreeCells();

        this.lockRoomIfNeeded();

        this.removeOutsideItems();

        if (this.lastItemCheckTime < currentTime - ADD_ITEM_INTERVAL * 1000) {
            this.lastItemCheckTime = currentTime;
            this.addNewItemIfNeeded();
        }
    }

    processPlayerPosition(player, pos) {
        if (!this.cells[pos.x] || !this.cells[pos.x][pos.y] || this.cells[pos.x][pos.y].pid != player.pid) {
            if (!this.pathCells[pos.x] || !this.pathCells[pos.x][pos.y]) {
                let cell: Cell = new Cell();
                cell.x = pos.x;
                cell.y = pos.y;
                cell.pid = player.pid;
                player.path_cells.push(cell);
                if (!this.pathCells[cell.x]) this.pathCells[cell.x] = {};
                this.pathCells[cell.x][cell.y] = cell;
                this.map[cell.x + MAP_SIZE][cell.y + MAP_SIZE] = 0;
            } else if (this.pathCells[pos.x][pos.y].pid != player.pid) {
                // owner e cell ro mizane
                let player2 = this.players[this.pathCells[pos.x][pos.y].pid];
                if (player2) {
                    if (!player.outside) {
                        if (player2.status == PLAYER_STATUS_NORMAL) {
                            if (this.pathCells[pos.x][pos.y].pid != player2.pid) {
                                throw new Error("fuck 1");
                            }
                            /*if(!player.ai || !player2.ai) */console.log(player.name + "(pid=" + player.pid + ") was not outside and just killed " + player2.name);

                            // console.log("colormetas:");
                            // for (let i = 0; i < this.maxClients; i++) {
                            //     let cm = this.state.colorMeta[i];
                            //     if (!cm) continue;
                            //     console.log("pid=" + cm.pid + " player.name=" + (cm.player ? cm.player.name : "nulllllllll"));
                            // }

                            // console.log(player2.name + "'s path cells are:");
                            // let found = false;
                            // for (var i = 0; i < player2.cells.length; i++) {
                            //     console.log(" path cell : " + player2.cells[i].x + ", " + player2.cells[i].y);
                            //     if(player2.cells[i].x == pos.x && player2.cells[i].y == pos.y) found = true;
                            // }
                            // if(!found) {
                            //     console.log(pos.x + ", " + pos.y + " is not in player " + player2.name + " cells :/");
                            //     throw new Error("KIRRRRRRRRRRRRRRRRRRRRRRR");
                            // }

                            this.hitPlayer(player, player2);
                        }
                    } else {
                        if (player.status == PLAYER_STATUS_NORMAL) {
                            /*if(!player.ai || !player2.ai)*/console.log("owwww " + player.name + "(pid=" + player.pid + ") should'nt be out there alone! he was outside and " + player2.name + "(pid=" + player2.pid + ") killed him");

                            // console.log("colormetas:");
                            // for (let i = 0; i < this.maxClients; i++) {
                            //     let cm = this.state.colorMeta[i];
                            //     if (!cm) continue;
                            //     console.log("pid=" + cm.pid + " player.name=" + (cm.player ? cm.player.name : "nulllllllll"));
                            // }

                            this.hitPlayer(player2, player);
                        }
                    }
                }
            }

            if (player.home || this.skipPlayerPath <= 0) {
                let x = player.x > (MAP_SIZE_X_PIXEL + 0.5 * GRID_WIDTH) ? (MAP_SIZE_X_PIXEL + 0.5 * GRID_WIDTH) : (player.x < -MAP_SIZE_X_PIXEL ? -MAP_SIZE_X_PIXEL : player.x);
                let y = player.y > MAP_SIZE_Y_PIXEL ? MAP_SIZE_Y_PIXEL : (player.y < -MAP_SIZE_Y_PIXEL ? -MAP_SIZE_Y_PIXEL : player.y);
                if (player.lastPoint) {
                    let dx = x - player.lastPoint.x;
                    let dy = y - player.lastPoint.y;
                    if (dx * dx + dy * dy > 225) {
                        let point: Point = new Point();
                        point.x = x;
                        point.y = y;
                        player.path.push(point);
                        player.lastPoint = point;
                    }
                } else {
                    let point: Point = new Point();
                    point.x = x;
                    point.y = y;
                    player.path.push(point);
                    player.lastPoint = point;
                }
            }

            player.home = false;
        } else {

            // home sweet home

            player.home = true;

            if (this.pathCells[pos.x] && this.pathCells[pos.x][pos.y] && this.pathCells[pos.x][pos.y].pid != player.pid) {
                // yeki dge umade too khunash
                // owner e path cell ro mizane
                let player2 = this.players[this.pathCells[pos.x][pos.y].pid];
                if (player2) {
                    if (player2.status == PLAYER_STATUS_NORMAL) {
                        if (!player2.ai) {
                            // age man ro zadan
                            console.log("pos = " + pos.x + "," + pos.y);
                            // console.log("colormetas:");
                            // for (let i = 0; i < this.maxClients; i++) {
                            //     let cm = this.state.colorMeta[i];
                            //     if (!cm) continue;
                            //     console.log("pid=" + cm.pid + " player.name=" + (cm.player ? cm.player.name : "nulllllllll"));
                            // }
                        }
                        /*if(!player.ai || !player2.ai) */console.log(player.name + "(pid=" + player.pid + ") killed " + player2.name + "(pid=" + player2.pid + ") just because they were at his/her home!");
                        this.hitPlayer(player, player2);
                    }
                }
            }

            // az biroon miad too khune
            if (player.path_cells.length > 0) {

                let list = [];
                this.fill = [];
                this.fillNumber = 1;
                this.fillOutsideAreas = [];
                this.fillQueue = [];
                // console.log("player x: [" + player.minX + " --- " + player.maxX + "], [" + player.minY + " --- " + player.maxY + "]");
                for (let y = player.minY; y <= player.maxY; y++) {
                    for (let x = player.minX; x <= player.maxX; x++) {
                        if (this.goodToGo(player, x, y)) {
                            this.fillQueue.push({ x: x, y: y });
                            while (this.fillQueue.length > 0) {
                                let item = this.fillQueue.pop();
                                this.fillPoint(player, item.x, item.y);
                            }
                        }
                        this.fillNumber++;
                    }
                }

                for (let y = player.minY; y <= player.maxY; y++) {
                    for (let x = player.minX; x <= player.maxX; x++) {
                        if (this.fill[x] && this.fill[x][y]) {
                            //console.log("outside is ",this.outsideAreas);
                            //console.log("(x, y) = (" + x + ", " + y + ")")
                            if (this.fillOutsideAreas.includes(this.fill[x][y])) continue;
                            list.push({ x: x, y: y });
                        }
                    }
                }

                // path cell hasham be khunash ezafe mikonim
                player.path_cells.forEach((cell, key) => {
                    if (!this.pathCells[cell.x]) return;
                    if (!this.pathCells[cell.x][cell.y]) return;
                    list.push({ x: cell.x, y: cell.y });
                });

                let numCapturedCells = list.length;

                list.forEach(item => {

                    if (this.cells[item.x] && this.cells[item.x][item.y]) {
                        // age khune por bud
                        let cell: Cell = this.cells[item.x][item.y];
                        let owner: Player = this.players[cell.pid];
                        if (cell.pid != player.pid) {
                            // age in cell ghablan male harif bude

                            // add new cell to player.cells
                            let newCell: Cell = new Cell();
                            newCell.x = item.x;
                            newCell.y = item.y;
                            newCell.pid = player.pid;
                            player.cells.set('k' + item.x * 10000 + item.y, newCell);
                            player.numCells++;

                            this.cells[item.x][item.y] = newCell;

                            // remove cell from owner.cells and remove owner if they lost their home
                            if (owner && !owner.dead) {
                                owner.numCells--;
                                owner.cells.delete('k' + item.x * 10000 + item.y);

                                if (owner.numCells <= 0) {
                                    // dead
                                    player.kills++;
                                    console.log("RIP " + owner.name + ", clientId= " + owner.clientId + ", pid = " + owner.pid);
                                    this.removePlayer(owner, true, this.getHexPosition(player.x, player.y));
                                }
                            }
                        }
                    } else {
                        if (!this.cells[item.x]) this.cells[item.x] = {};
                        let cell: Cell = new Cell();
                        cell.x = item.x;
                        cell.y = item.y;
                        cell.pid = player.pid;
                        this.cells[item.x][item.y] = cell;
                        player.cells.set('k' + item.x * 10000 + item.y, cell);
                        player.numCells++;
                    }

                    this.map[item.x + MAP_SIZE][item.y + MAP_SIZE] = 0;
                });

                player.path_cells.forEach((cell, key) => {
                    if (this.pathCells[cell.x]) this.pathCells[cell.x][cell.y] = undefined;
                    // TODO: niazi be in check nist chon msiri ke rafte baad bargashte be khune alan joze khunashe
                    if (!this.cells[cell.x] || !this.cells[cell.x][cell.y]) {
                        this.map[cell.x + MAP_SIZE][cell.y + MAP_SIZE] = 1;
                    }
                });

                player.path_cells.clear()
                player.path.clear()

                this.broadcast("capture", { player: player.clientId, num: numCapturedCells });
            }
        }
    }

    fillPoint(player, x, y) {
        // console.log('processing ' + x + ' ,' + y);

        if (x < player.minX || x > player.maxX || y < player.minY || y > player.maxY) {
            if (!this.fillOutsideAreas.includes(this.fillNumber))
                this.fillOutsideAreas.push(this.fillNumber);
            return;
        }

        if (!this.fill[x]) this.fill[x] = {};
        this.fill[x][y] = this.fillNumber;

        if (this.goodToGo(player, x + 1, y)) this.fillQueue.push({ x: x + 1, y: y });
        if (this.goodToGo(player, x - 1, y)) this.fillQueue.push({ x: x - 1, y: y });
        if (this.goodToGo(player, x, y + 1)) this.fillQueue.push({ x: x, y: y + 1 });
        if (this.goodToGo(player, x, y - 1)) this.fillQueue.push({ x: x, y: y - 1 });

        if (y % 2 == 0) {
            if (this.goodToGo(player, x - 1, y - 1)) this.fillQueue.push({ x: x - 1, y: y - 1 });
            if (this.goodToGo(player, x - 1, y + 1)) this.fillQueue.push({ x: x - 1, y: y + 1 });
        } else {
            if (this.goodToGo(player, x + 1, y - 1)) this.fillQueue.push({ x: x + 1, y: y - 1 });
            if (this.goodToGo(player, x + 1, y + 1)) this.fillQueue.push({ x: x + 1, y: y + 1 });
        }
    }

    goodToGo(player, x, y) {
        if (this.fill[x] && this.fill[x][y]) return false;
        if (this.cells[x] && this.cells[x][y] && this.cells[x][y].pid == player.pid) return false;
        if (this.pathCells[x] && this.pathCells[x][y] && this.pathCells[x][y].pid == player.pid) return false;
        return true;
    }

    getHexPosition(x, y) {
        let pos = { x: 0, y: 0 };
        pos.x = x * GRID_WIDTH + (y % 2 == 0 ? 0 : GRID_WIDTH / 2);
        pos.y = y * GRID_HEIGHT;
        return pos;
    }

    positionToHex(x, y) {
        var yi = Math.floor((y + GRID_HEIGHT / 2) / GRID_HEIGHT);
        var xi = yi % 2 == 0 ? Math.floor((x + GRID_WIDTH / 2) / GRID_WIDTH) : Math.floor(x / GRID_WIDTH);
        return { x: xi, y: yi };
    }

    addBot() {
        // let pid = -1;
        // for (let i = 1; i <= this.maxClients; i++) {
        //     if (this.players[i]) continue;
        //     pid = i;
        //     break;
        // }
        // if (pid == -1) return;

        let pid = ++this.currentPid;

        let bot: Player = new Player();
        bot.pid = pid;
        bot.clientId = "bot_" + Math.floor(Math.random() * 1000000);
        let color = getFillStrokePair(pid);
        if (BOTS_CAN_HAVE_TEXTURE && Math.random() > 0.5) {
            bot.fill = getRandomTexture();
        } else {
            bot.fill = color.fill;
        }
        bot.stroke = color.stroke;
        this.players[pid] = bot;
        let recInfo = maximalRectangle(this.map);
        // console.log(recInfo);
        let x = Math.floor((recInfo.x1 + recInfo.x2 - MIN_WIDTH) / 2) - MAP_SIZE;
        let y = Math.floor((recInfo.y1 + recInfo.y2 - MIN_HEIGHT) / 2) - MAP_SIZE;
        let pos = this.add_cells(x, y, pid);
        pos = this.getHexPosition(pos.x + 1, pos.y + 1);
        bot.x = pos.x
        bot.y = pos.y;
        bot.new_angle = bot.angle = Math.random() * 2 * Math.PI;
        bot.ai = new TimeBasedAI(this, bot, MAP_SIZE_X_PIXEL, MAP_SIZE_Y_EXT_PIXEL);
        bot.name = generateUsername();
        this.state.players.set(bot.clientId, bot)
        this.lastJoinTime = Date.now();
    }

    updateNumFreeCells() {
        this.numFreeCells = 0;
        for (let i = 0; i < 2 * MAP_SIZE + 1; i++) {
            for (let j = 0; j < 2 * MAP_SIZE + 1; j++) {
                this.numFreeCells += this.map[i][j];
            }
        }
    }

    hitPlayer(playerA, playerB) {
        console.log(playerA.clientId + "(" + playerA.name + ") just killed " + playerB.clientId + " (" + playerB.name + ")");

        if (playerA.client) playerA.client.send("hit", ""); // you just hit somebody playerA

        playerA.kills++;

        // playerB is dead

        console.log("RIP " + playerB.name + ", clientId= " + playerB.clientId + ", pid = " + playerB.pid);
        this.removePlayer(playerB, true, { x: playerA.x, y: playerA.y });
    }

    findRandomPointAngleInHome(player) {
        let point = this.getRandomPointInPlayerHome(player.pid);
        if (point == null) return { point: { x: 0, y: 0 }, angle: 0 };
        point = this.getHexPosition(point.x, point.y);
        let angle = Math.random() * 2 * Math.PI;
        return { point: point, angle: angle }
    }

    getRandomPointInPlayerHome(pid) {
        let list = [];
        for (let y = -MAP_SIZE; y <= MAP_SIZE; y++) {
            for (let x = -MAP_SIZE; x <= MAP_SIZE; x++) {
                if (this.cells[x] && this.cells[x][y] && this.cells[x][y].pid == pid)
                    list.push({ x: x, y: y });
            }
        }
        // console.log('list is ', list);
        if (list.length == 0) return null;
        return list[Math.floor(Math.random() * list.length)];
    }

    processPlayerAi(player) {
        if (!player.ai) return;
        player.new_angle = player.ai.getAngle();
    }

    removePlayer(player: Player, letThemKnow: boolean = false, killerPosition: { x: number, y: number } = { x: 0, y: 0 }) {
        console.log("removing player " + player.clientId + "(" + player.name + ")");
        player.path_cells.forEach((cell, key) => {
            if (this.pathCells[cell.x]) {
                this.pathCells[cell.x][cell.y] = undefined;
                // console.log("setting this.pathCells[" + cell.x + "][" + cell.y + "] = undefined");
            }
            if (!this.cells[cell.x] || !this.cells[cell.x][cell.y]) {
                this.map[cell.x + MAP_SIZE][cell.y + MAP_SIZE] = 1;
            }
        });

        // remove player cells
        player.cells.forEach((cell, key) => {
            if (!cell) return;
            if (this.cells[cell.x]) this.cells[cell.x][cell.y] = undefined;
            if (!this.pathCells[cell.x] || !this.pathCells[cell.x][cell.y]) {
                this.map[cell.x + MAP_SIZE][cell.y + MAP_SIZE] = 1;
            }
            //delete player.cells[key];
        });

        // let player know that they are removed
        if (letThemKnow) {
            if (player.client) {
                let client = player.client;
                this.clock.setTimeout(() => {
                    if (client) client.leave();
                }, PLAYER_CLIENT_CLOSE_TIMEOUT * 1000);
                client.send("death", { x: killerPosition.x, y: killerPosition.y }); // death message
            } else {
                // console.log("sorry but player " + player.name + " has no client. -_-");
            }
        }
        player.dead = true;
        //player.path.length = 0;
        //player.path_cells.length = 0;
        delete this.players[player.pid];
        this.state.players.delete(player.clientId)
    }

    lockRoomIfNeeded() {
        if (this.numHumanPlayers < this.maxClients && this.numFreeCells >= JOIN_FREE_CELLS_PERCENT * TOTAL_CELLS) {
            if (this.locked) this.unlock();
        } else if (!this.locked) this.lock();
    }

    checkIfPlayerEatItem(player: Player) {
        this.state.items.forEach((item, key) => {
            if ((player.x - item.x) * (player.x - item.x) + Math.abs(player.y - item.y) * Math.abs(player.y - item.y) < ITEM_EAT_DISTANCE * ITEM_EAT_DISTANCE) {
                if (item.type == Item.TYPE_COIN) {
                    // add coins to player record
                    if (player.uid) {
                        // get(CONSTANTS.api + "/user/" + player.uid, { headers: { 'Accept': 'application/json' } }).then(response => {
                        //     let coins = response.data.coins + COIN_ITEM_ADD_VALUE;
                        //     return patch(CONSTANTS.api + "/user/" + player.uid, { headers: { 'Accept': 'application/json' }, body: { coins: coins } }).then(response => {
                        //         return coins;
                        //     });
                        // }).then(coins => {
                        //     if (player.client) {
                        //         this.send(player.client, { op: 'et', type: item.type, coins: coins, add: COIN_ITEM_ADD_VALUE });
                        //     }
                        //     delete this.state.items[key];
                        // }).catch(e => {
                        //     console.log(e);
                        // });
                        player.coins += COIN_ITEM_ADD_VALUE
                        this.state.items.delete(key);
                    } else {
                        this.state.items.delete(key);
                    }
                } else if (item.type == Item.TYPE_BOOST) {
                    // let's speed this bitch up
                    player.boost = true;
                    player.boost_time = Date.now();
                    player.speed = PLAYER_SPEED_BOOST;
                    if (player.client) {
                        player.client.send("eat", { type: item.type });
                    }
                    this.state.items.delete(key);
                }
            }
        });
    }

    removeOutsideItems() {
        this.state.items.forEach((item, key) => {
            if (item.x <= MAP_SIZE_X_EXT_PIXEL && item.x >= -MAP_SIZE_X_EXT_PIXEL) return;
            if (item.y <= MAP_SIZE_Y_EXT_PIXEL && item.y >= -MAP_SIZE_Y_EXT_PIXEL) return;
            // remove item
            this.state.items.delete(key);
        });
    }

    addNewItemIfNeeded() {
        var numCoinItems = 0
        this.state.items.forEach((item, key) => { if (key.charAt(0) == 'c') numCoinItems++ });

        if (numCoinItems < NUM_TOTAL_COINS) {
            let pos = this.getNewRandomItemPosition();
            let item: Item = new Item();
            item.x = pos.x;
            item.y = pos.y;
            item.type = Item.TYPE_COIN;
            this.state.items.set("c_" + this.currentItemKey++, item)
        }

        var numBoostItems = 0
        this.state.items.forEach((item, key) => { 
            if (key.charAt(0) == 'b') numBoostItems++ });

        if (numBoostItems < NUM_TOTAL_BOOSTS) {
            let pos = this.getNewRandomItemPosition();
            let item: Item = new Item();
            item.x = pos.x;
            item.y = pos.y;
            item.type = Item.TYPE_BOOST;
            this.state.items.set("b_" + this.currentItemKey++, item);
        }
    }

    getNewRandomItemPosition() {
        let array = [];
        for (let i = 0; i < 2 * MAP_SIZE + 1; i++) {
            array[i] = [];
            for (let j = 0; j < 2 * MAP_SIZE + 1; j++) array[i][j] = 1;
        }

        // avoid players
        Object.keys(this.players).forEach(pid => {
            let player: Player = this.players[pid];
            if (!player.xi || !player.yi) return;
            // console.log("player.xi = " + player.xi + ", yi = " + player.yi);
            array[player.xi + MAP_SIZE][player.yi + MAP_SIZE] = 0;
        });

        // avoid other items
        this.state.items.forEach((item, key) => {
            let yi = Math.floor((item.y + GRID_HEIGHT / 2) / GRID_HEIGHT);
            if (yi > MAP_SIZE || yi < -MAP_SIZE) return;
            let xi = yi % 2 == 0 ? Math.floor((item.x + GRID_WIDTH / 2) / GRID_WIDTH) : Math.floor(item.x / GRID_WIDTH);
            if (xi > MAP_SIZE || xi < -MAP_SIZE) return;
            array[xi + MAP_SIZE][yi + MAP_SIZE] = 0;
        });

        let recInfo = maximalRectangle(array);
        // console.log("new item ***************************")
        // console.log("recInfo:");
        // console.log(recInfo);
        let x = Math.floor((recInfo.x1 + recInfo.x2) / 2) - MAP_SIZE;
        let y = Math.floor((recInfo.y1 + recInfo.y2) / 2) - MAP_SIZE;

        // TODO: maybe randomize x,y

        let ret = { x: 0, y: 0 };
        ret.x = x * GRID_WIDTH + (y % 2 == 0 ? 0 : GRID_WIDTH / 2);
        ret.y = y * GRID_HEIGHT;

        // console.log(" >>>>>>> " + ret.x + ", " + ret.y);

        return ret;
    }

    /*********************************************** DEBUG ************************************************ */

    drawCells() {
        console.log("--------------------------------------------")
        for (let y = MAP_SIZE; y >= -MAP_SIZE; y--) {
            var line = "";
            for (let x = -MAP_SIZE; x <= MAP_SIZE; x++) {
                line += this.cells[x] ? (this.cells[x][y] ? this.cells[x][y].pid : " ") : " ";
            }
            console.log(line);
        }
    }

    drawPathCells() {
        console.log("--------------------------------------------")
        for (let y = MAP_SIZE; y >= -MAP_SIZE; y--) {
            var line = "";
            for (let x = -MAP_SIZE; x <= MAP_SIZE; x++) {
                line += this.pathCells[x] ? (this.pathCells[x][y] ? this.pathCells[x][y].pid : " ") : " ";
            }
            console.log(line);
        }
    }

    drawPlayerPositions() {
        let list = [];
        console.log("--------------------------------------------")
        this.state.players.forEach((player, key) => {
            if (!player) return;
            list[player.pid] = { x: player.xi, y: player.yi };
        });
        for (let y = MAP_SIZE; y >= -MAP_SIZE; y--) {
            var line = "";
            for (let x = -MAP_SIZE; x <= MAP_SIZE; x++) {
                let aaa = -1;
                for (let i = 0; i < this.maxClients; i++) {
                    if (list[i] && list[i].x == x && list[i].y == y) {
                        aaa = i;
                        break;
                    }
                }
                line += aaa == -1 ? " " : aaa;
            }
            console.log(line);
        }
    }

    // validatePathCells() {
    //     for (let i = 1; i <= this.maxClients; i++) {
    //         let player = this.players[i];
    //         if (!player) continue;
    //         if (player.pid != i) {
    //             throw new Error("fuck you fix this -_- player.pid != i");
    //         }
    //         for (let x = -MAP_SIZE; x <= MAP_SIZE; x++) {
    //             if (!this.pathCells[x]) continue;
    //             for (let y = -MAP_SIZE; y <= MAP_SIZE; y++) {
    //                 if (!this.pathCells[x][y]) continue;
    //                 if (this.pathCells[x][y].pid == player.pid) {
    //                     let found = false;
    //                     for (let j = 0; j < player.path_cells.length; j++) {
    //                         if (player.path_cells[j].x == x && player.path_cells[j].y == y) {
    //                             found = true;
    //                             break;
    //                         }
    //                     }
    //                     if (!found) {
    //                         console.log(" >> " + this.pathCells[x][y].pid)
    //                         console.log(" >> " + player.pid);
    //                         console.log("not found path cell with x = " + x + ", y = " + y + " in " + player.name + "'s cells");
    //                         console.log(player.name + "'s path cells:");
    //                         for (let j = 0; j < player.path_cells.length; j++) {
    //                             console.log(player.path_cells[j].x + ", " + player.path_cells[j].y + " pid=" + player.path_cells[j].pid);
    //                         }
    //                         this.drawPathCells();
    //                         throw new Error("kir too kunet kos kesh bug dari.... -_-");
    //                     }
    //                 }
    //             }
    //         }
    //         for (let j = 0; j < player.path_cells.length; j++) {
    //             if (!this.pathCells[player.path_cells[j].x]) throw new Error("khawk be saret ke " + player.path_cells[j].x + ", " + player.path_cells[j].y + " too path cells nist");
    //             if (!this.pathCells[player.path_cells[j].x][player.path_cells[j].y]) throw new Error("khawk be saret ke " + player.path_cells[j].x + ", " + player.path_cells[j].y + " too path cells nist");
    //             if (this.pathCells[player.path_cells[j].x][player.path_cells[j].y].pid != player.pid) throw new Error("khawk be saret ke " + player.path_cells[j].x + ", " + player.path_cells[j].y + " rangesh bayad " + player.pid + " bashe vali " + this.pathCells[player.path_cells[j].x][player.path_cells[j].y].pid + " e");
    //         }
    //     }
    // }
}