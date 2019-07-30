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
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
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

import io.colyseus.Client;
import io.colyseus.Room;
import io.colyseus.serializer.schema.DataChange;
import ir.doorbash.hexy.model.Cell;
import ir.doorbash.hexy.model.ColorMeta;
import ir.doorbash.hexy.model.MyState;
import ir.doorbash.hexy.model.Player;
import ir.doorbash.hexy.model.Point;
import ir.doorbash.hexy.util.ColorUtil;
import ir.doorbash.hexy.util.ConfigFile;
import ir.doorbash.hexy.util.PathCellUpdate;

/**
 * Created by Milad Doorbash on 7/22/2019.
 */
public class PlayScreen extends ScreenAdapter {

    /* *************************************** CONSTANTS *****************************************/

    private static final boolean DEBUG_SHOW_GHOST = false;

    private static final boolean CORRECT_PLAYER_POSITION = true;
    private static final boolean ADD_FAKE_PATH_CELLS = false;

    private static final String ENDPOINT = "ws://192.168.1.134:3333";
//    public static final String ENDPOINT = "ws://46.21.147.7:3333";
//    public static final String ENDPOINT = "ws://127.0.0.1:3333";

    private static final String PATH_LOG_FONT = "fonts/NotoSans-Regular.ttf";
    private static final String PATH_PACK_ATLAS = "pack.atlas";
    private static final String PATH_TRAIL_TEXTURE = "traine.png";
    private static final String TEXTURE_REGION_HEX_WHITE = "hex_white";
    private static final String TEXTURE_REGION_THUMBSTICK_BG = "thumbstick-background";
    private static final String TEXTURE_REGION_THUMBSTICK_PAD = "thumbstick-pad";
    private static final String TEXTURE_REGION_BC = "bc";
    private static final String TEXTURE_REGION_INDIC = "indic";

    private static final int CONTROLLER_TYPE_MOUSE = 1;
    private static final int CONTROLLER_TYPE_PAD = 2;
    private static final int CONTROLLER_TYPE_ON_SCREEN = 3;

    private static final int CORRECT_PLAYER_POSITION_INTERVAL = 100;

    private static final int SEND_DIRECTION_INTERVAL = 200;
    private static final int SEND_PING_INTERVAL = 5000;

    private static final float CAMERA_LERP = 0.3f;
    private static final float CAMERA_INIT_ZOOM = 0.8f;

    private static final float PATH_CELL_ALPHA_TINT = 0.4f;

    private static final int PAD_CONTROLLER_MAX_LENGTH = 42;
    private static final float ON_SCREEN_PAD_RELEASE_TOTAL_TIME = 0.3f;
    private static final Interpolation ON_SCREEN_PAD_RELEASE_ELASTIC_OUT = new Interpolation.ElasticOut(3, 2, 3, 0.5f);

    private static final int PATH_CELLS_UPDATE_TIME = 500;

    private static final float GRID_WIDTH = 44;
    private static final float GRID_HEIGHT = 38;

    private static final int CELL_GRID_WIDTH = 100;
    private static final int CELL_GRID_HEIGHT = 100;

    private static final int MAP_SIZE = 20;
    private static final int EXTENDED_CELLS = 3;

    private static final int TOTAL_CELLS = (2 * MAP_SIZE + 1) * (2 * MAP_SIZE + 1);
    private static final float MAP_SIZE_X_PIXEL = (MAP_SIZE * GRID_WIDTH);
    private static final float MAP_SIZE_X_EXT_PIXEL = (MAP_SIZE + EXTENDED_CELLS) * GRID_WIDTH;
    private static final float MAP_SIZE_Y_PIXEL = (MAP_SIZE * GRID_HEIGHT);
    private static final float MAP_SIZE_Y_EXT_PIXEL = (MAP_SIZE + EXTENDED_CELLS) * GRID_HEIGHT;


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
    private BitmapFont usernameFont;
    //    private SimpleMesh simpleMesh;
    private Texture trailTexture;
//    private TrailGraphic trailGraphic;

    /* **************************************** FIELDS *******************************************/

    private Client client;
    private Room<MyState> room;
    private long timeDiff;
    private float lastDirection;
    private float direction;
    private LinkedHashMap<String, Object> message = new LinkedHashMap<>();
    private int correctPlayerPositionTime = CORRECT_PLAYER_POSITION_INTERVAL;
    private int sendDirectionTime = SEND_DIRECTION_INTERVAL;
    private int sendPingTime = SEND_PING_INTERVAL;
    private final ArrayList<Player> players = new ArrayList<>();
    private final HashMap<Integer, Cell> cells = new HashMap<>();
    private final Cell[][] cellGrid = new Cell[CELL_GRID_WIDTH][];
    private final Cell[][] pathCellGrid = new Cell[CELL_GRID_WIDTH][];
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

    /* ************************************** CONSTRUCTOR ****************************************/

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
        camera.zoom = CAMERA_INIT_ZOOM;
        controllerCamera = new OrthographicCamera();
        controllerCamera.setToOrtho(true, screenWidth, screenHeight);
        viewportControllerCam = new ExtendViewport(screenWidth, screenHeight, controllerCamera);
        controllerCamera.update();
        guiCamera = new OrthographicCamera();
        guiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        guiCamera.update();

        trailTexture = new Texture(Gdx.files.internal(PATH_TRAIL_TEXTURE), true);
        trailTexture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        trailTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        initFonts();

        initTiles();

        initInput();

        connectToServer();
    }

    /* *************************************** OVERRIDE *****************************************/

    @Override
    public void render(float dt) {
        Gdx.gl.glClearColor(0.92f, 0.92f, 0.92f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl20.glEnable(GL20.GL_BLEND);
        Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

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
            updateZoom();

            drawCells();
            drawPaths();
            batch.end();
            drawTrails();
            batch.begin();
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
        logFont.draw(batch, logText, 8, 4 + logFont.getLineHeight());

        batch.end();

        if (sendDirectionTime < 0) {
            sendDirection();
            sendDirectionTime = SEND_DIRECTION_INTERVAL;
        } else sendDirectionTime -= dt * 1000;

        if (sendPingTime < 0) {
            sendPing();
            sendPingTime = SEND_PING_INTERVAL;
        } else sendPingTime -= dt * 1000;

//        simpleMesh.render(guiCamera.combined);

//        trailGraphic.render(camera.combined);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        viewportControllerCam.update(width, height);
        guiCamera.viewportWidth = width;
        guiCamera.viewportHeight = height;
        guiCamera.update();
    }

    @Override
    public void dispose() {
        batch.dispose();
        fbo.dispose();
        gameAtlas.dispose();
        logFont.dispose();
        usernameFont.dispose();
        freetypeGenerator.dispose();
    }

    /* ***************************************** INIT *******************************************/

    private void initFonts() {
        freetypeGenerator = new FreeTypeFontGenerator(Gdx.files.internal(PATH_LOG_FONT));
        FreeTypeFontGenerator.FreeTypeFontParameter logFontParams = new FreeTypeFontGenerator.FreeTypeFontParameter();
        logFontParams.size = 14 * Gdx.graphics.getWidth() / screenWidth;
        logFontParams.color = Color.BLACK;
        logFontParams.flip = false;
        logFontParams.incremental = true;
        logFont = freetypeGenerator.generateFont(logFontParams);

        FreeTypeFontGenerator.FreeTypeFontParameter usernameFontParams = new FreeTypeFontGenerator.FreeTypeFontParameter();
        usernameFontParams.size = 24 * Gdx.graphics.getWidth() / screenWidth;
        usernameFontParams.color = Color.BLACK;
        usernameFontParams.flip = false;
        usernameFontParams.incremental = true;
//        usernameFontParams.genMipMaps = true;
//        usernameFontParams.minFilter = Texture.TextureFilter.Linear;
//        usernameFontParams.magFilter = Texture.TextureFilter.MipMapLinearLinear;
        usernameFont = freetypeGenerator.generateFont(usernameFontParams);
    }

    private void initTiles() {
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, 2 * screenWidth, 2 * screenHeight, false);
        fbo.begin();

        Gdx.gl.glClearColor(0.92f, 0.92f, 0.92f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Gdx.gl20.glEnable(GL20.GL_BLEND);
        Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        Matrix4 m = new Matrix4();
        m.setToOrtho2D(0, 0, fbo.getWidth(), fbo.getHeight());

        batch.setProjectionMatrix(m);

        batch.begin();

        for (int xi = -3; xi < 22; xi++) {
            for (int yi = -3; yi < 37; yi++) {
                batch.draw(whiteHex, xi * GRID_WIDTH + (yi % 2 == 0 ? 0 : GRID_WIDTH / 2f) - 20, yi * GRID_HEIGHT - 23, 40, 46);
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

    /* ***************************************** DRAW *******************************************/

    private void drawTiles() {
        float actualWidth = camera.zoom * camera.viewportWidth;
        float actualHeight = camera.zoom * camera.viewportHeight;

        float leftX = camera.position.x - actualWidth / 2f;
        float topY = camera.position.y - actualHeight / 2f;

        int bottomYi = (int) Math.floor((topY + GRID_HEIGHT / 2f) / GRID_HEIGHT) - 1;
        int leftXi = (int) (bottomYi % 2 == 0 ? Math.floor((leftX + GRID_WIDTH / 2f) / GRID_WIDTH) : Math.floor(leftX / GRID_WIDTH)) - 1;

        float firstX = leftXi * GRID_WIDTH + (bottomYi % 2 == 0 ? 0 : GRID_WIDTH / 2f);
        float firstY = bottomYi * GRID_HEIGHT;

        tiles.setX(firstX);
        tiles.setY(firstY);
        tiles.draw(batch);
    }

    private void drawPaths() {
        synchronized (players) {
            for (Player player : players) {
                if (player != null && player.status == 0) {
                    synchronized (player.pathCells) {
                        for (Cell cell : player.pathCells.values()) {
                            if (cell != null && cell.id != null) {
                                cell.id.draw(batch);
                            }
                        }
                    }
                }
            }
        }
    }

    private void drawTrails() {
        synchronized (players) {
            for (Player player : players) {
                if (player != null && player.status == 0 && player.trailGraphic != null) {
                    player.trailGraphic.render(batch.getProjectionMatrix());
                }
            }
        }
    }

    private void drawCells() {
        synchronized (cells) {
            for (Cell cell : cells.values()) {
                if (cell != null && cell.id != null) {
                    cell.id.draw(batch);
                }
            }
        }
    }

    private void drawPlayers() {
        synchronized (players) {
            for (Player player : players) {
                if (player != null && player.status == 0) {
                    if (DEBUG_SHOW_GHOST && player.bcGhost != null) player.bcGhost.draw(batch);
                    if (player.bc != null) player.bc.draw(batch);
                    if (player.c != null) player.c.draw(batch);
                    if (player.indic != null) player.indic.draw(batch);
                    if (player.bc != null) {
                        float x = player.bc.getX() + player.bc.getWidth() / 2f - player.text.width / 2f;
                        float y = player.bc.getY() + 70;
                        usernameFont.draw(batch, player.name, x, y);
                    }
                }
            }
        }
    }

    private void clearPlayerPath(String clientId) {
        Player player = room.getState().players.get(clientId);
        if (player != null) {
            Gdx.app.postRunnable(() -> player.trailGraphic.truncateAt(0));
            synchronized (player.pathCells) {
                player.pathCells.clear();
            }
            if (ADD_FAKE_PATH_CELLS) {
                player.pathCellUpdates.clear();
                synchronized (pathCellGrid) {
                    for (int i = 0; i < CELL_GRID_WIDTH; i++) {
                        for (int j = 0; j < CELL_GRID_HEIGHT; j++) {
                            if (pathCellGrid[i] != null && pathCellGrid[i][j] != null && pathCellGrid[i][j].color == player.color) {
                                pathCellGrid[i][j] = null;
                            }
                        }
                    }
                }
            }
        }
    }

    /* ***************************************** LOGIC *******************************************/

    private void updatePlayersPositions(float dt) {
        synchronized (players) {
            for (Player player : players) {
                if (player.bc != null && player.status == 0) {

                    if (ADD_FAKE_PATH_CELLS) {
                        synchronized (player.pathCells) {
                            while (true) {
                                PathCellUpdate update = player.pathCellUpdates.peek();
                                if (update == null) break;
                                if (update.time > (System.currentTimeMillis() - PATH_CELLS_UPDATE_TIME))
                                    break;
                                synchronized (pathCellGrid) {
                                    Cell cell;
                                    if ((cell = player.pathCells.get(update.key)) != null) {
                                        if (pathCellGrid[cell.x + CELL_GRID_WIDTH / 2] != null && pathCellGrid[cell.x + CELL_GRID_WIDTH / 2][cell.y + CELL_GRID_HEIGHT / 2] != null) {
                                            pathCellGrid[cell.x + CELL_GRID_WIDTH / 2][cell.y + CELL_GRID_HEIGHT / 2] = null;
                                        }

                                    }
                                    if (pathCellGrid[update.cell.x + CELL_GRID_WIDTH / 2] == null)
                                        pathCellGrid[update.cell.x + CELL_GRID_WIDTH / 2] = new Cell[CELL_GRID_HEIGHT];
                                    pathCellGrid[update.cell.x + CELL_GRID_WIDTH / 2][update.cell.y + CELL_GRID_HEIGHT / 2] = update.cell;
                                }
                                player.pathCells.put(update.key, update.cell);
                                player.pathCellUpdates.pop();
                            }
                        }
                    }

                    if (CORRECT_PLAYER_POSITION) {
                        if (correctPlayerPositionTime < 0) {
                            correctPlayerPosition(player);
                            correctPlayerPositionTime = CORRECT_PLAYER_POSITION_INTERVAL;
                        } else correctPlayerPositionTime -= dt * 1000;
                    }

                    float x = player.bc.getX() + player.bc.getWidth() / 2f;
                    float y = player.bc.getY() + player.bc.getHeight() / 2f;

                    float newX = x + MathUtils.cos(player.angle) * player.speed * dt;
                    float newY = y + MathUtils.sin(player.angle) * player.speed * dt;

                    if (newX <= MAP_SIZE_X_EXT_PIXEL && newX >= -MAP_SIZE_X_EXT_PIXEL) x = newX;
                    if (newY <= MAP_SIZE_Y_EXT_PIXEL && newY >= -MAP_SIZE_Y_EXT_PIXEL) y = newY;

                    player.bc.setCenter(x, y);

                    player.c.setCenter(x, y);

                    if (player.indic != null) {
                        player.indic.setCenter(x, y);
                        player.indic.setRotation(player.angle * MathUtils.radiansToDegrees - 90);
                    }

                    player.bcGhost.setCenter(player.x, player.y);

                    if (ADD_FAKE_PATH_CELLS) {
                        if (room.getState().started && !room.getState().ended)
                            processPlayerPosition(player, x, y);
                    }
                }
            }
        }
    }

    private void processPlayerPosition(Player player, float x, float y) {
        int yi = (int) Math.floor((y + GRID_HEIGHT / 2f) / GRID_HEIGHT);
        int xi = (int) (yi % 2 == 0 ? Math.floor((x + GRID_WIDTH / 2f) / GRID_WIDTH) : Math.floor(x / GRID_WIDTH));

//        System.out.println("player " + player.clientId + " is at " + xi + ", " + yi);

        if (cellGrid[xi + CELL_GRID_WIDTH / 2] == null || cellGrid[xi + CELL_GRID_WIDTH / 2][yi + CELL_GRID_HEIGHT / 2] == null || cellGrid[xi + CELL_GRID_WIDTH / 2][yi + CELL_GRID_HEIGHT / 2].color != player.color) {
            synchronized (pathCellGrid) {
                if (pathCellGrid[xi + CELL_GRID_WIDTH / 2] == null || pathCellGrid[xi + CELL_GRID_WIDTH / 2][yi + CELL_GRID_HEIGHT / 2] == null) {
                    Cell cell = new Cell();
                    //cell.owner = player.clientId;
                    cell.color = player.color;
                    cell.x = (short) xi;
                    cell.y = (short) yi;
                    cell.id = gameAtlas.createSprite(TEXTURE_REGION_HEX_WHITE);
                    cell.id.setSize(40, 46);
                    Vector2 pos = getHexPosition(cell.x, cell.y);
                    cell.id.setCenter(pos.x, pos.y);
                    Color color = ColorUtil.bc_color_index_to_rgba[cell.color - 1];
                    cell.id.setColor((1 - PATH_CELL_ALPHA_TINT) + PATH_CELL_ALPHA_TINT * color.r, (1 - PATH_CELL_ALPHA_TINT) + PATH_CELL_ALPHA_TINT * color.g, (1 - PATH_CELL_ALPHA_TINT) + PATH_CELL_ALPHA_TINT * color.b, 1.0f);

                    if (pathCellGrid[xi + CELL_GRID_WIDTH / 2] == null)
                        pathCellGrid[xi + CELL_GRID_WIDTH / 2] = new Cell[CELL_GRID_HEIGHT];
                    pathCellGrid[xi + CELL_GRID_WIDTH / 2][yi + CELL_GRID_HEIGHT / 2] = cell;
                    synchronized (player.pathCells) {
                        player.pathCells.put(player.pathCells.size(), cell);
                    }
                } /*else if (!pathCellGrid[xi + CELL_GRID_WIDTH / 2][yi + CELL_GRID_HEIGHT / 2].owner.equals(player.clientId)) {
                    // path cell male yeki dgas
                }*/
            }
        } else {
            // in home
            clearPlayerPath(player.clientId);
        }
    }

    private void updateZoom() {
        Player player = room.getState().players.get(client.getId());
        if (player == null) return;
        ColorMeta cm = room.getState().colorMeta.get(player.color + "");
        if (cm == null) return;
        float percentage = cm.numCells / (float) TOTAL_CELLS;
//        System.out.println("percentage = " + percentage);
        camera.zoom = CAMERA_INIT_ZOOM + 0.8f * percentage;
    }

    private long getServerTime() {
        return System.currentTimeMillis() + timeDiff;
    }

    private void correctPlayerPosition(Player player) {
        float dst = Vector2.dst2(player.bc.getX() + player.bc.getWidth() / 2f, player.bc.getY() + player.bc.getHeight() / 2f, player.x, player.y);

        float d;
        if (dst > 5625) {
            d = dst;
        } else if (dst > 625) {
            d = 100f;
        } else if (dst > 130) {
            d = 10f;
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

    private Vector2 getHexPosition(int x, int y) {
        Vector2 pos = new Vector2();
        pos.x = x * GRID_WIDTH + (y % 2 == 0 ? 0 : GRID_WIDTH / 2f);
        pos.y = y * GRID_HEIGHT;
        return pos;
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

    /* **************************************** NETWORK ******************************************/

    private void connectToServer() {
        client = new Client(ENDPOINT, ConfigFile.get("clientId"), null, null, 10000, new Client.Listener() {
            @Override
            public void onOpen(String id) {
                ConfigFile.set("clientId", id);
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
//                        System.out.println("onMessage()");
//                        System.out.println(message);
                        LinkedHashMap<String, Object> data = (LinkedHashMap<String, Object>) message;
                        if (data.get("op").equals("wlc")) {
                            long time = (long) data.get("time");
                            timeDiff = time - System.currentTimeMillis();
                            System.out.println("time diff: " + timeDiff);
                        } else if (data.get("op").equals("cp")) {
                            String clientId = (String) data.get("player");
                            clearPlayerPath(clientId);

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

                            Gdx.app.postRunnable(() -> {
                                player.text = new GlyphLayout(usernameFont, player.name);

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
                                    camera.position.x = player.x;
                                    camera.position.y = player.y;
                                }

                                player.trailGraphic = new TrailGraphic(trailTexture);
                                player.trailGraphic.setTint(bcColor);
                                player.trailGraphic.setRopeWidth(20);
                                player.trailGraphic.setTextureULengthBetweenPoints(1 / 2f);
                            });

                            player.path.onAddListener = (point, key2) -> {
                                Gdx.app.postRunnable(() -> {
                                    if (key2 > 1) {
                                        Point lastPoint = player.path.get(key2 - 1);
                                        if (lastPoint != null) {
                                            float dx = point.x - lastPoint.x;
                                            float dy = point.y - lastPoint.y;
                                            player.trailGraphic.setPoint(key2 * 2 - 1, lastPoint.x + dx / 2f, lastPoint.y + dy / 2f);
                                        }
                                        player.trailGraphic.setPoint(key2 * 2, point.x, point.y);
                                    } else if (key2 == 1) {
                                        Point lastPoint = player.path.get(0);
                                        if (lastPoint != null) {
                                            float dx = point.x - lastPoint.x;
                                            float dy = point.y - lastPoint.y;
                                            player.trailGraphic.setPoint(0, lastPoint.x - dx / 2f, lastPoint.y - dy / 2f);
                                            player.trailGraphic.setPoint(1, lastPoint.x + dx / 2f, lastPoint.y + dy / 2f);
                                        }
                                        player.trailGraphic.setPoint(2, point.x, point.y);
                                    }
                                });
                            };

                            player.cells.onAddListener = (cell, key2) -> {
//                                player.pathCellsLock.lock();
//                                player.pathCells.put(key2, cell);
//                                player.pathCellsLock.unlock();

                                cell.id = gameAtlas.createSprite(TEXTURE_REGION_HEX_WHITE);
                                cell.id.setSize(40, 46);
                                Vector2 pos = getHexPosition(cell.x, cell.y);
                                cell.id.setCenter(pos.x, pos.y);
                                Color color = ColorUtil.bc_color_index_to_rgba[cell.color - 1];
                                cell.id.setColor((1 - PATH_CELL_ALPHA_TINT) + PATH_CELL_ALPHA_TINT * color.r, (1 - PATH_CELL_ALPHA_TINT) + PATH_CELL_ALPHA_TINT * color.g, (1 - PATH_CELL_ALPHA_TINT) + PATH_CELL_ALPHA_TINT * color.b, 1.0f);

                                if (ADD_FAKE_PATH_CELLS) {
                                    player.pathCellUpdates.offer(new PathCellUpdate(cell, key2, System.currentTimeMillis()));
                                } else {
                                    synchronized (player.pathCells) {
                                        player.pathCells.put(key2, cell);
                                    }
                                }
                            };
//                            player.cells.onRemoveListener = (cell, key2) -> {
//                                player.pathCellsLock.lock();
//                                player.pathCells.remove(key2);
//                                player.pathCellsLock.unlock();
//                            };

                            player.path.triggerAll();
                            player.cells.triggerAll();
                        };
                        room.getState().players.onRemoveListener = (player, key) -> {
                            synchronized (players) {
                                players.remove(player);
                            }
                        };

                        room.getState().cells.onAddListener = (cell, key) -> {
                            synchronized (cells) {
                                cells.put(key, cell);
                            }
                            if (cellGrid[cell.x + CELL_GRID_WIDTH / 2] == null)
                                cellGrid[cell.x + CELL_GRID_WIDTH / 2] = new Cell[CELL_GRID_HEIGHT];
                            cellGrid[cell.x + CELL_GRID_WIDTH / 2][cell.y + CELL_GRID_HEIGHT / 2] = cell;

                            cell.id = gameAtlas.createSprite(TEXTURE_REGION_HEX_WHITE);
                            cell.id.setSize(40, 46);
                            Vector2 pos = getHexPosition(cell.x, cell.y);
                            cell.id.setCenter(pos.x, pos.y);
                            cell.id.setColor(ColorUtil.bc_color_index_to_rgba[cell.color - 1]);

                            cell.onChange = changes -> cell.id.setColor(ColorUtil.bc_color_index_to_rgba[cell.color - 1]);
                        };
                        room.getState().cells.onRemoveListener = (cell, key) -> {
                            synchronized (cells) {
                                cells.remove(key);
                            }
                            if (cellGrid[cell.x + CELL_GRID_WIDTH / 2] != null) {
                                cellGrid[cell.x + CELL_GRID_WIDTH / 2][cell.y + CELL_GRID_HEIGHT / 2] = null;
                            }
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

    private void sendDirection() {
        if (room == null || !room.hasJoined()) return;
        if (lastDirection != direction) {
            message.put("op", "d");
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
}