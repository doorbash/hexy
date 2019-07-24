package ir.doorbash.hexy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import io.colyseus.Client;
import io.colyseus.Room;
import io.colyseus.serializer.schema.DataChange;
import ir.doorbash.hexy.model.Cell;
import ir.doorbash.hexy.model.MyState;
import ir.doorbash.hexy.model.Player;
import ir.doorbash.hexy.util.ColorUtil;

/**
 * Created by Milad Doorbash on 7/22/2019.
 */
public class PlayScreen extends ScreenAdapter {

    private static final boolean DEBUG_SHOW_GHOST = false;

//    private static final String ENDPOINT = "ws://192.168.1.134:3333";
    public static final String ENDPOINT = "ws://46.21.147.7:3333";
//    public static final String ENDPOINT = "ws://127.0.0.1:3333";

    private static final String PATH_LOG_FONT = "fonts/NotoSans-Regular.ttf";
    private static final String PATH_PACK_ATLAS = "pack.atlas";
    private static final String TEXTURE_REGION_HEX_WHITE = "hex_white";
    private static final String TEXTURE_REGION_THUMBSTICK_BG = "thumbstick-background";
    private static final String TEXTURE_REGION_THUMBSTICK_PAD = "thumbstick-pad";
    private static final String TEXTURE_REGION_BC = "bc";
    private static final String TEXTURE_REGION_INDIC = "indic";

    private static final int CONTROLLER_TYPE_MOUSE = 1;
    private static final int CONTROLLER_TYPE_PAD = 2;
    private static final int CONTROLLER_TYPE_ON_SCREEN = 3;

    private static final boolean CORRECT_PLAYER_POSITION = true;
    private static final int CORRECT_PLAYER_POSITION_INTERVAL = 100;

    private static final int SEND_DIRECTION_INTERVAL = 200;
    private static final int SEND_PING_INTERVAL = 5000;

    private static final float CAMERA_LERP = 0.9f;

    private static final float PATH_CELL_ALPHA_TINT = 0.4f;

    private static final int PAD_CONTROLLER_MAX_LENGTH = 42;
    private static final float ON_SCREEN_PAD_RELEASE_TOTAL_TIME = 0.3f;
    private static final Interpolation ON_SCREEN_PAD_RELEASE_ELASTIC_OUT = new Interpolation.ElasticOut(3, 2, 3, 0.5f);


    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Viewport viewport;
    private Viewport viewportControllerCam;
    private TextureAtlas gameAtlas;
    private FrameBuffer fbo;
    private Sprite tiles;
    private TextureAtlas.AtlasRegion whiteHex;
    private Sprite thumbstickBgSprite;
    private Sprite thumbstickPadSprite;
    private FreeTypeFontGenerator freetypeGenerator;
    private BitmapFont logFont;

    private Client client;
    private Room<MyState> room;
    private long timeDiff;
    private float lastDirection;
    private float direction;
    private LinkedHashMap<String, Object> message = new LinkedHashMap<>();
    private int correctPlayerPositionTime = CORRECT_PLAYER_POSITION_INTERVAL;
    private int sendDirectionTime = SEND_DIRECTION_INTERVAL;
    private int sendPingTime = 0;
    private final ArrayList<Player> players = new ArrayList<>();
    private final HashMap<Integer, Cell> cells = new HashMap<>();
    private Lock cellsLock = new ReentrantLock();
    private int controllerType = CONTROLLER_TYPE_PAD;
    private OrthographicCamera controllerCamera;
    private OrthographicCamera guiCamera;
    private boolean mouseIsDown = false;
    private Vector2 padAnchorPoint = new Vector2();
    private Vector2 padVector = new Vector2();
    private Vector2 onScreenPadNorVector = new Vector2();
    private float onScreenPadCurrentLen = 0;
    private float onScreenPadReleaseTimer = 0;
    private float onScreenPadInitLen = 0;
    private int screenWidth = 480;
    private int screenHeight;
    private Vector2 onScreenPadPosition;
    private long lastPingTime;
    private int currentPing;

    PlayScreen() {
        screenHeight = screenWidth * Gdx.graphics.getHeight() / Gdx.graphics.getWidth();
        onScreenPadPosition = new Vector2(screenWidth - 120, screenHeight - 120);
        batch = new SpriteBatch();
        gameAtlas = new TextureAtlas(PATH_PACK_ATLAS);
        whiteHex = gameAtlas.findRegion(TEXTURE_REGION_HEX_WHITE);
        thumbstickBgSprite = gameAtlas.createSprite(TEXTURE_REGION_THUMBSTICK_BG);
        thumbstickBgSprite.setSize(152, 152);
        thumbstickBgSprite.setCenter(onScreenPadPosition.x, onScreenPadPosition.y);
        thumbstickPadSprite = gameAtlas.createSprite(TEXTURE_REGION_THUMBSTICK_PAD);
        thumbstickPadSprite.setSize(70, 70);
        thumbstickPadSprite.setCenter(onScreenPadPosition.x, onScreenPadPosition.y);
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(screenWidth, screenHeight, camera);
        camera.zoom = 1f;
        controllerCamera = new OrthographicCamera();
        controllerCamera.setToOrtho(true, screenWidth, screenHeight);
        viewportControllerCam = new ExtendViewport(screenWidth, screenHeight, controllerCamera);
        controllerCamera.update();
        guiCamera = new OrthographicCamera();
        guiCamera.setToOrtho(true, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        guiCamera.update();

        initFonts();

        initTiles();

        initInput();

        connectToServer();
    }

    @Override
    public void render(float dt) {
        Gdx.gl.glClearColor(0.92f, 0.92f, 0.92f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (room != null) {
            Player player = room.getState().players.get(client.getId());
            if (player != null && player.bc != null) {
//                camera.position.x = player.bc.getX() + player.bc.getWidth() / 2f;
//                camera.position.y = player.bc.getY() + player.bc.getHeight() / 2f;
                camera.position.x = MathUtils.lerp(camera.position.x, player.bc.getX() + player.bc.getWidth() / 2f, CAMERA_LERP);
                camera.position.y = MathUtils.lerp(camera.position.y, player.bc.getY() + player.bc.getHeight() / 2f, CAMERA_LERP);
            }
        }
        camera.update();

        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        drawTiles();
        if (room != null) {
            updatePlayersPositions(dt);

            drawCells();
            drawPathCells();
            drawPlayers();
        }

        if (controllerType != CONTROLLER_TYPE_MOUSE) {
            batch.setProjectionMatrix(controllerCamera.combined);

            if (controllerType == CONTROLLER_TYPE_ON_SCREEN && !mouseIsDown && !MathUtils.isEqual(onScreenPadCurrentLen, 0)) {
//            System.out.println("bouncing....");
                onScreenPadReleaseTimer += dt;
                onScreenPadCurrentLen = (1 - ON_SCREEN_PAD_RELEASE_ELASTIC_OUT.apply(Math.min(1, onScreenPadReleaseTimer / ON_SCREEN_PAD_RELEASE_TOTAL_TIME))) * onScreenPadInitLen;
//            System.out.println("current pad length is " + onScreenPadCurrentLen);
                padVector.set(onScreenPadNorVector.x * onScreenPadCurrentLen, onScreenPadNorVector.y * onScreenPadCurrentLen);
                thumbstickPadSprite.setCenter(onScreenPadPosition.x + padVector.x, onScreenPadPosition.y + padVector.y);
            }

            if ((controllerType == CONTROLLER_TYPE_PAD && mouseIsDown) || controllerType == CONTROLLER_TYPE_ON_SCREEN) {
                thumbstickBgSprite.draw(batch);
                thumbstickPadSprite.draw(batch);
            }
        }

        batch.setProjectionMatrix(guiCamera.combined);

        String logText = "fps: " + Gdx.graphics.getFramesPerSecond();
        if (currentPing != 0) {
            logText += " - ping: " + currentPing;
        }
        logFont.draw(batch, logText, 8, Gdx.graphics.getHeight() - 4 - logFont.getLineHeight());

        batch.end();

        if (sendDirectionTime < 0) {
            sendDirection();
            sendDirectionTime = SEND_DIRECTION_INTERVAL;
        } else sendDirectionTime -= dt * 1000;

        if (sendPingTime < 0) {
            sendPing();
            sendPingTime = SEND_PING_INTERVAL;
        } else sendPingTime -= dt * 1000;
    }

    @Override
    public void dispose() {
        batch.dispose();
        fbo.dispose();
        gameAtlas.dispose();
        logFont.dispose();
        freetypeGenerator.dispose();
    }

    public void resize(int width, int height) {
        viewport.update(width, height);
        viewportControllerCam.update(width, height);
        guiCamera.viewportWidth = width;
        guiCamera.viewportHeight = height;
        guiCamera.update();
    }

    private void drawTiles() {
        float actualWidth = camera.zoom * camera.viewportWidth;
        float actualHeight = camera.zoom * camera.viewportHeight;

        float leftX = camera.position.x - actualWidth / 2f;
        float topY = camera.position.y - actualHeight / 2f;

        int bottomYi = (int) Math.floor((topY + 19f) / 38f) - 1;
        int leftXi = (int) (bottomYi % 2 == 0 ? Math.floor((leftX + 22) / 44f) : Math.floor(leftX / 44f)) - 1;

        float firstX = leftXi * 44 + (bottomYi % 2 == 0 ? 0 : 22);
        float firstY = bottomYi * 38;

        tiles.setX(firstX);
        tiles.setY(firstY);
        tiles.draw(batch);
    }

    private void drawPlayers() {
        synchronized (players) {
            for (Player player : players) {
                if (player != null && player.status == 0) {
                    if (DEBUG_SHOW_GHOST && player.bcGhost != null) player.bcGhost.draw(batch);
                    if (player.bc != null) player.bc.draw(batch);
                    if (player.c != null) player.c.draw(batch);
                    if (player.indic != null) player.indic.draw(batch);
                }
            }
        }
    }

    private void drawPathCells() {
        synchronized (players) {
            for (Player player : players) {
                if (player != null && player.status == 0) {
                    if (player.pathCellsLock.tryLock()) {
                        try {
                            for (Cell cell : player.pathCells.values()) {
                                if (cell != null && cell.id != null) {
                                    cell.id.draw(batch);
                                }
                            }
                        } finally {
                            player.pathCellsLock.unlock();
                        }
                    }
                }
            }
        }
    }

    private void drawCells() {
        cellsLock.lock();
        for (Cell cell : cells.values()) {
            if (cell != null && cell.id != null) {
                cell.id.draw(batch);
            }
        }
        cellsLock.unlock();
    }

    private void updatePlayersPositions(float dt) {
        synchronized (players) {
            for (Player player : players) {
                if (player.bc != null && player.status == 0) {

                    if (CORRECT_PLAYER_POSITION) {
                        if (correctPlayerPositionTime < 0) {
                            correctPlayerPosition(player);
                            correctPlayerPositionTime = CORRECT_PLAYER_POSITION_INTERVAL;
                        } else correctPlayerPositionTime -= dt * 1000;
                    }

                    player.bc.translate(MathUtils.cos(player.angle) * player.speed * dt, MathUtils.sin(player.angle) * player.speed * dt);
                    player.c.setCenter(player.bc.getX() + player.bc.getWidth() / 2f, player.bc.getY() + player.bc.getHeight() / 2f);

                    if (player.indic != null) {
                        player.indic.setCenter(player.bc.getX() + player.bc.getWidth() / 2f, player.bc.getY() + player.bc.getHeight() / 2f);
                        player.indic.setRotation(player.angle * MathUtils.radiansToDegrees - 90);
                    }

                    player.bcGhost.setCenter(player.x, player.y);
                }
            }
        }
    }

    private void correctPlayerPosition(Player player) {
        float dst = Vector2.dst2(player.bc.getX() + player.bc.getWidth() / 2f, player.bc.getY() + player.bc.getHeight() / 2f, player.x, player.y);

        float d;
        if (dst > 5625) {
            d = dst;
        } else if (dst > 625) {
            d = 25;
        } else if (dst > 100) {
            d = 5f;
        } else {
            if (player.clientId.equals(client.getId())) System.out.println("NO LERPING");
            return;
        }

//        float d = dst > 75 ? dst : dst > 25 ? 5f : Math.min(dst, 1.25f);
        if (player.clientId.equals(client.getId())) {
            System.out.println("sqrt is " + dst);
            System.out.println("lerp is " + d / dst);
        }

        if (dst > 0 && d > 0) {
            player.bc.setCenterX(MathUtils.lerp(player.bc.getX() + player.bc.getWidth() / 2f, player.x, d / dst));
            player.bc.setCenterY(MathUtils.lerp(player.bc.getY() + player.bc.getHeight() / 2f, player.y, d / dst));
            player.c.setCenter(player.bc.getX() + player.bc.getWidth() / 2f, player.bc.getY() + player.bc.getHeight() / 2f);
            if (player.indic != null) {
                player.indic.setCenter(player.bc.getX() + player.bc.getWidth() / 2f, player.bc.getY() + player.bc.getHeight() / 2f);
            }
        }
    }

    private void connectToServer() {
        client = new Client(ENDPOINT, null, null, null, 10000, new Client.Listener() {
            @Override
            public void onOpen(String s) {
                LinkedHashMap<String, Object> options = new LinkedHashMap<>();
                options.put("name", "milad");
                room = client.join("public_1", options, MyState.class);
                room.addListener(new Room.Listener() {
                    @Override
                    protected void onLeave() {
                        System.out.println("left public_1");
                    }

                    @Override
                    protected void onError(Exception e) {
                        System.out.println("onError()");
                        e.printStackTrace();
                    }

                    @Override
                    protected void onMessage(Object message) {
                        System.out.println("onMessage()");
                        System.out.println(message);
                        LinkedHashMap<String, Object> data = (LinkedHashMap<String, Object>) message;
                        if (data.get("op").equals("wlc")) {
                            long time = (long) data.get("time");
                            timeDiff = time - System.currentTimeMillis();
                            System.out.println("time diff: " + timeDiff);
                        } else if (data.get("op").equals("cp")) {
//                            String clientId = (String) data.get("player");
                        } else if (data.get("op").equals("pg")) {
                            long t = (long) data.get("t");
                            if (t == lastPingTime) {
                                currentPing = (int) (System.currentTimeMillis() - t);
                            } else currentPing = 0;
                        }
                    }

                    @Override
                    protected void onJoin() {
                        System.out.println("joined public_1");
                        room.getState().onChange = changes -> {
                            for (DataChange change : changes) {
                                switch (change.field) {
                                    case "started":
                                        break;
                                    case "startTime":
                                        System.out.println("value changed to " + change.value);
                                        break;
                                    case "ended":
                                        System.out.println("value changed to " + change.value);
                                        break;
                                    case "endTime":
                                        System.out.println("value changed to " + change.value);
                                        break;
                                }
                            }
                        };
                        room.getState().players.onAddListener = (player, key) -> {
                            synchronized (players) {
                                players.add(player);
                            }
                            Color bcColor = ColorUtil.bc_color_index_to_rgba[player.color - 1];
                            Color cColor = ColorUtil.c_color_index_to_rgba[player.color - 1];

                            player.bc = gameAtlas.createSprite(TEXTURE_REGION_BC);
                            player.bc.setSize(46, 46);
                            player.bc.setColor(bcColor);
                            player.bc.setCenter(player.x, player.y);

                            player.c = gameAtlas.createSprite(TEXTURE_REGION_BC);
                            player.c.setSize(36, 36);
                            player.c.setColor(cColor);
                            player.c.setCenter(player.x, player.y);

                            if (player.clientId.equals(client.getId())) {
                                player.indic = gameAtlas.createSprite(TEXTURE_REGION_INDIC);
                                player.indic.setSize(80, 80);
                                player.indic.setColor(bcColor);
                                player.indic.setCenter(player.x, player.y);
                                player.indic.setOriginCenter();
                                player.indic.setRotation(player.angle * MathUtils.radiansToDegrees - 90);
                            }

                            player.bcGhost = gameAtlas.createSprite(TEXTURE_REGION_BC);
                            player.bcGhost.setColor(bcColor.r, bcColor.g, bcColor.b, bcColor.a / 2f);
                            player.bcGhost.setCenter(player.x, player.y);
                            player.bcGhost.setSize(46, 46);

                            if (player.clientId.equals(client.getId())) {
                                // this is you
                                camera.position.x = player.x;
                                camera.position.y = player.y;
                            }
                            player.cells.onAddListener = (cell, key2) -> {
                                player.pathCellsLock.lock();
                                player.pathCells.put(key2, cell);
                                player.pathCellsLock.unlock();
                                cell.id = gameAtlas.createSprite(TEXTURE_REGION_HEX_WHITE);
                                cell.id.setSize(40, 46);
                                Vector2 pos = getHexPosition(cell.x, cell.y);
                                cell.id.setCenter(pos.x, pos.y);
                                Color color = ColorUtil.bc_color_index_to_rgba[cell.color - 1];
                                cell.id.setColor((1 - PATH_CELL_ALPHA_TINT) + PATH_CELL_ALPHA_TINT * color.r, (1 - PATH_CELL_ALPHA_TINT) + PATH_CELL_ALPHA_TINT * color.g, (1 - PATH_CELL_ALPHA_TINT) + PATH_CELL_ALPHA_TINT * color.b, 1.0f);
                            };
                            player.cells.onRemoveListener = (cell, key2) -> {
                                player.pathCellsLock.lock();
                                player.pathCells.remove(key2);
                                player.pathCellsLock.unlock();
                            };
                        };

                        room.getState().players.onRemoveListener = (player, key) -> {
                            synchronized (players) {
                                players.remove(player);
                            }
                        };

                        room.getState().cells.onAddListener = (cell, key) -> {
                            cellsLock.lock();
                            cells.put(key, cell);
                            cellsLock.unlock();
                            cell.id = gameAtlas.createSprite(TEXTURE_REGION_HEX_WHITE);
                            cell.id.setSize(40, 46);
                            Vector2 pos = getHexPosition(cell.x, cell.y);
                            cell.id.setCenter(pos.x, pos.y);
                            cell.id.setColor(ColorUtil.bc_color_index_to_rgba[cell.color - 1]);
                            cell.onChange = changes -> cell.id.setColor(ColorUtil.bc_color_index_to_rgba[cell.color - 1]);
                        };
                        room.getState().cells.onRemoveListener = (cell, key) -> {
                            cellsLock.lock();
                            cells.remove(key);
                            cellsLock.unlock();
                        };
                    }
                });
            }

            @Override
            public void onMessage(Object o) {

            }

            @Override
            public void onClose(int i, String s, boolean b) {

            }

            @Override
            public void onError(Exception e) {

            }
        });
    }

    private long getServerTime() {
        return System.currentTimeMillis() + timeDiff;
    }

    private void sendDirection() {
        if (room == null || !room.hasJoined()) return;
        if (lastDirection != direction) {
            message.put("op","d");
            message.put("v", direction);
            room.send(message);
            lastDirection = direction;
        }
    }

    private void sendPing() {
        if (room == null || !room.hasJoined()) return;
        lastPingTime = System.currentTimeMillis();
        message.put("op", "p");
        message.put("v", lastPingTime);
        room.send(message);
    }

    private Vector2 getHexPosition(int x, int y) {
        Vector2 pos = new Vector2();
        pos.x = x * 44 + (y % 2 == 0 ? 0 : 22);
        pos.y = y * 38;
        return pos;
    }

    private void initTiles() {
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, 2 * screenWidth, 2 * screenHeight, false);
        fbo.begin();

        Gdx.gl.glClearColor(0.92f, 0.92f, 0.92f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Matrix4 m = new Matrix4();
        m.setToOrtho2D(0, 0, fbo.getWidth(), fbo.getHeight());

        batch.setProjectionMatrix(m);

        batch.begin();

        for (int xi = -3; xi < 22; xi++) {
            for (int yi = -3; yi < 37; yi++) {
                batch.draw(whiteHex, xi * 44 + (yi % 2 == 0 ? 0 : 22) - 20, yi * 38 - 23, 40, 46);
            }
        }

        batch.end();

        fbo.end();

        tiles = new Sprite(fbo.getColorBufferTexture());

        tiles.flip(false, true);
    }

    private void initInput() {
        Gdx.input.setInputProcessor(new InputProcessor() {
            @Override
            public boolean keyDown(int keycode) {
                return false;
            }

            @Override
            public boolean keyUp(int keycode) {
                return false;
            }

            @Override
            public boolean keyTyped(char character) {
                return false;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
//                System.out.println("TouchDown");
                mouseIsDown = true;
                screenX = screenX * screenWidth / Gdx.graphics.getWidth();
                screenY = screenY * screenHeight / Gdx.graphics.getHeight();
                padAnchorPoint.set(screenX, screenY);
                handleTouchDownDrag(screenX, screenY);
                return true;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
//                System.out.println("TouchUp");
                mouseIsDown = false;
                return true;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
//                System.out.println("touchDragged()");
                screenX = screenX * screenWidth / Gdx.graphics.getWidth();
                screenY = screenY * screenHeight / Gdx.graphics.getHeight();
                handleTouchDownDrag(screenX, screenY);
                return true;
            }

            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                return false;
            }

            @Override
            public boolean scrolled(int amount) {
                return false;
            }
        });
    }

    private void handleTouchDownDrag(int screenX, int screenY) {
        if (controllerType == CONTROLLER_TYPE_MOUSE) {
            float dx = screenX - screenWidth / 2f;
            float dy = screenY - screenHeight / 2f;
            direction = (int) Math.toDegrees(Math.atan2(-dy, dx));
        } else if (controllerType == CONTROLLER_TYPE_PAD && mouseIsDown) {
            thumbstickBgSprite.setCenter(padAnchorPoint.x, padAnchorPoint.y);
            padVector.set(screenX - padAnchorPoint.x, screenY - padAnchorPoint.y);
            if (padVector.len2() > PAD_CONTROLLER_MAX_LENGTH * PAD_CONTROLLER_MAX_LENGTH) {
                padVector.nor().scl(PAD_CONTROLLER_MAX_LENGTH);
            }
            thumbstickPadSprite.setCenter(padAnchorPoint.x + padVector.x, padAnchorPoint.y + padVector.y);
            direction = (int) Math.toDegrees(Math.atan2(-padVector.y, padVector.x));
        } else if (controllerType == CONTROLLER_TYPE_ON_SCREEN) {
            padVector.set(screenX - onScreenPadPosition.x, screenY - onScreenPadPosition.y);
            onScreenPadInitLen = padVector.len();
            onScreenPadNorVector = padVector.nor().cpy();
            if (onScreenPadInitLen > PAD_CONTROLLER_MAX_LENGTH) {
                onScreenPadInitLen = PAD_CONTROLLER_MAX_LENGTH;
            }
            padVector.scl(onScreenPadInitLen);
            onScreenPadCurrentLen = onScreenPadInitLen;
            onScreenPadReleaseTimer = 0;
            thumbstickPadSprite.setCenter(onScreenPadPosition.x + padVector.x, onScreenPadPosition.y + padVector.y);
            direction = (int) Math.toDegrees(Math.atan2(-padVector.y, padVector.x));
        }
    }

    private void initFonts() {
        freetypeGenerator = new FreeTypeFontGenerator(Gdx.files.internal(PATH_LOG_FONT));
        FreeTypeFontGenerator.FreeTypeFontParameter logFontParameters = new FreeTypeFontGenerator.FreeTypeFontParameter();
        logFontParameters.size = 14 * Gdx.graphics.getWidth() / screenWidth;
        logFontParameters.color = Color.BLACK;
        logFontParameters.flip = true;
        logFontParameters.incremental = true;
        logFont = freetypeGenerator.generateFont(logFontParameters);
    }
}
