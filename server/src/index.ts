import express from 'express';
import { createServer } from 'http';
import { Server } from 'colyseus';
import rateLimit from "express-rate-limit";
import { FreeForAll } from "./rooms/FreeForAll"

export const CONSTANTS = {
    api: "http://localhost:3232"
};

const port = 2222;
const app = express();

app.use(express.json());


const apiLimiter = rateLimit({
    windowMs: 15 * 60 * 1000, // 15 minutes
    max: 100
});

app.use("/matchmake/", apiLimiter);
// see https://expressjs.com/en/guide/behind-proxies.html
// app.set('trust proxy', 1);

const gameServer = new Server({
    server: createServer(app),
    express: app,
});

// gameServer.register("battle", Battle);
gameServer.define("ffa", FreeForAll).sortBy({ clients: -1 });

gameServer.onShutdown(function () {
    console.log(`game server is going down.`);
});

gameServer.listen(port, '0.0.0.0');
console.log(`Listening on http://localhost:${port}`);