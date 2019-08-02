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
import com.crowni.gdx.rtllang.support.ArFont;
import com.crowni.gdx.rtllang.support.ArUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

//    private static final String ENDPOINT = "ws://192.168.1.134:3333";
    public static final String ENDPOINT = "ws://46.21.147.7:3333";
//    public static final String ENDPOINT = "ws://127.0.0.1:3333";

    private static final String PATH_FONT_NOTO = "fonts/NotoSans-Regular.ttf";
    private static final String PATH_FONT_ARIAL = "fonts/arialbd.ttf";

    private static final String PATH_PACK_ATLAS = "pack.atlas";
    private static final String PATH_TRAIL_TEXTURE = "traine.png";
    private static final String TEXTURE_REGION_HEX_WHITE = "hex_white";
    private static final String TEXTURE_REGION_THUMBSTICK_BG = "thumbstick-background";
    private static final String TEXTURE_REGION_THUMBSTICK_PAD = "thumbstick-pad";
    private static final String TEXTURE_REGION_BC = "bc";
    private static final String TEXTURE_REGION_INDIC = "indic";
    private static final String TEXTURE_REGION_PROGRESSBAR = "progressbar";

    private static final int CONTROLLER_TYPE_MOUSE = 1;
    private static final int CONTROLLER_TYPE_PAD = 2;
    private static final int CONTROLLER_TYPE_ON_SCREEN = 3;

    private static final int CORRECT_PLAYER_POSITION_INTERVAL = 100;

    private static final int SEND_DIRECTION_INTERVAL = 200;
    private static final int SEND_PING_INTERVAL = 5000;

    private static final float CAMERA_LERP = 0.8f;
    private static final float CAMERA_INIT_ZOOM = 0.9f;

    private static final float PATH_CELL_ALPHA_TINT = 0.4f;

    private static final int PAD_CONTROLLER_MAX_LENGTH = 42;
    private static final float ON_SCREEN_PAD_RELEASE_TOTAL_TIME = 0.3f;
    private static final Interpolation ON_SCREEN_PAD_RELEASE_ELASTIC_OUT = new Interpolation.ElasticOut(3, 2, 3, 0.5f);

//    private static final int PATH_CELLS_UPDATE_TIME = 500;

    private static final float GRID_WIDTH = 44;
    private static final float GRID_HEIGHT = 38;

    private static final int MAP_SIZE = 30;
    private static final int EXTENDED_CELLS = 3;

    private static final int TOTAL_CELLS = (2 * MAP_SIZE + 1) * (2 * MAP_SIZE + 1);
//    private static final float MAP_SIZE_X_PIXEL = (MAP_SIZE * GRID_WIDTH);
    private static final float MAP_SIZE_X_EXT_PIXEL = (MAP_SIZE + EXTENDED_CELLS) * GRID_WIDTH;
//    private static final float MAP_SIZE_Y_PIXEL = (MAP_SIZE * GRID_HEIGHT);
    private static final float MAP_SIZE_Y_EXT_PIXEL = (MAP_SIZE + EXTENDED_CELLS) * GRID_HEIGHT;

//    private static final int CELL_GRID_WIDTH = 2 * MAP_SIZE + 10;
//    private static final int CELL_GRID_HEIGHT = 2 * MAP_SIZE + 10;

    private static final int LEADERBOARD_NUM = 4;

    private static final int SCREEN_WIDTH_PORTRAIT = 480;
    private static final int SCREEN_WIDTH_LANDSCAPE = 800;

    private static final Color TEXT_BACKGROUND_COLOR = Color.valueOf("#707070cc");

    private static final float LEADERBORAD_CHANGE_SPEED = 50;

    private SpriteBatch batch;
    private OrthographicCamera camera;
    private TextureAtlas gameAtlas;
    private FrameBuffer fbo;
    private Sprite tiles;
    private TextureAtlas.AtlasRegion whiteHex;
    private Sprite thumbstickBgSprite;
    private Sprite thumbstickPadSprite;
    private FreeTypeFontGenerator freetypeGeneratorNoto;
    private FreeTypeFontGenerator freetypeGeneratorArial;
    private BitmapFont logFont;
    private BitmapFont usernameFont;
    private BitmapFont leaderboardFont;
    private BitmapFont timeFont;
    private Sprite timeBg;
    private Sprite youWillRspwnBg;
    private GlyphLayout timeText;
    private GlyphLayout youWillRspwnText;

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
//    private final Cell[][] cellGrid = new Cell[CELL_GRID_WIDTH][];
//    private final Cell[][] pathCellGrid = new Cell[CELL_GRID_WIDTH][];
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
    private int screenWidth;
    private int screenHeight;
    private Vector2 onScreenPadPosition = new Vector2();
    private long lastPingTime;
    private int currentPing;
    private final ArrayList<ColorMeta> colorMetas = new ArrayList<>();
    private Comparator<ColorMeta> colorMetaComp = (o1, o2) -> Integer.compare(o1._position, o2._position);
    private float progressbarWidth;
    private float progressbarHeight;
    private float progressbarTopMargin;
    private float progressbarGap;
    private float progressbarInitWidth;
    private float progressbarExtraGapForCurrentPlayer;
    private Player[] playersByColor = new Player[100];
    private float guiUnits;
    private ArFont arFont = new ArFont();

    /* ************************************** CONSTRUCTOR ****************************************/

    PlayScreen() {
        batch = new SpriteBatch();
        gameAtlas = new TextureAtlas(PATH_PACK_ATLAS);
        whiteHex = gameAtlas.findRegion(TEXTURE_REGION_HEX_WHITE);
        thumbstickBgSprite = gameAtlas.createSprite(TEXTURE_REGION_THUMBSTICK_BG);
        thumbstickBgSprite.setSize(152, 152);
        thumbstickPadSprite = gameAtlas.createSprite(TEXTURE_REGION_THUMBSTICK_PAD);
        thumbstickPadSprite.setSize(70, 70);
        timeBg = gameAtlas.createSprite(TEXTURE_REGION_PROGRESSBAR);
        youWillRspwnBg = gameAtlas.createSprite(TEXTURE_REGION_PROGRESSBAR);
        youWillRspwnBg.setColor(TEXT_BACKGROUND_COLOR);
        timeBg.setColor(TEXT_BACKGROUND_COLOR);
        camera = new OrthographicCamera();
        camera.zoom = CAMERA_INIT_ZOOM;
        controllerCamera = new OrthographicCamera();
        guiCamera = new OrthographicCamera();

        init(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        updateGuiValues();
//        trailTexture = new Texture(Gdx.files.internal(PATH_TRAIL_TEXTURE), true);
//        trailTexture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
//        trailTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        initFonts();

        timeText = new GlyphLayout(timeFont, "99:99");
        youWillRspwnText = new GlyphLayout(timeFont, "You will respawn in 9 seconds");

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

        if (room != null && room.getState().started) {
            drawLeaderboard(dt);
            if (!room.getState().ended) {
                drawTime();
                drawYouWillRespawnText();
            }
        }

        String logText = "fps: " + Gdx.graphics.getFramesPerSecond();
        if (currentPing > 0) {
            logText += " - ping: " + currentPing;
        }
        logFont.draw(batch, logText, -Gdx.graphics.getWidth() / 2f + 8 * guiUnits, -Gdx.graphics.getHeight() / 2f + 2 * guiUnits + logFont.getLineHeight());

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
        System.out.println("resize(" + width + ", " + height + ")");
        init(width, height);

        camera.viewportWidth = screenWidth;
        camera.viewportHeight = screenHeight;
        camera.update();

        controllerCamera.viewportWidth = screenWidth;
        controllerCamera.viewportHeight = screenHeight;
        controllerCamera.update();

        guiCamera.viewportWidth = width;
        guiCamera.viewportHeight = height;
        guiCamera.update();

        updateGuiValues();
    }

    @Override
    public void dispose() {
        batch.dispose();
        fbo.dispose();
        gameAtlas.dispose();
        logFont.dispose();
        usernameFont.dispose();
        leaderboardFont.dispose();
        timeFont.dispose();
        freetypeGeneratorNoto.dispose();
        freetypeGeneratorArial.dispose();
    }

    /* ***************************************** INIT *******************************************/

    private void init(int width, int height) {
        screenWidth = height > width ? SCREEN_WIDTH_PORTRAIT : SCREEN_WIDTH_LANDSCAPE;
        screenHeight = screenWidth * height / width;
        onScreenPadPosition.set(screenWidth / 2f - 120, -screenHeight / 2f + 120);
        if (controllerType == CONTROLLER_TYPE_ON_SCREEN) {
            thumbstickBgSprite.setCenter(onScreenPadPosition.x, onScreenPadPosition.y);
            thumbstickPadSprite.setCenter(onScreenPadPosition.x, onScreenPadPosition.y);
        }
    }

    private void initFonts() {
        freetypeGeneratorNoto = new FreeTypeFontGenerator(Gdx.files.internal(PATH_FONT_NOTO));
        freetypeGeneratorArial = new FreeTypeFontGenerator(Gdx.files.internal(PATH_FONT_ARIAL));

        FreeTypeFontGenerator.FreeTypeFontParameter logFontParams = new FreeTypeFontGenerator.FreeTypeFontParameter();
        logFontParams.size = 14 * Gdx.graphics.getWidth() / screenWidth;
        logFontParams.color = Color.BLACK;
        logFontParams.flip = false;
        logFontParams.incremental = true;
        logFont = freetypeGeneratorNoto.generateFont(logFontParams);

        FreeTypeFontGenerator.FreeTypeFontParameter leaderboardFontParams = new FreeTypeFontGenerator.FreeTypeFontParameter();
        leaderboardFontParams.characters += ArUtils.getAllChars().toString("");
        leaderboardFontParams.size = 16 * Gdx.graphics.getWidth() / screenWidth;
        leaderboardFontParams.color = new Color(0.8f, 0.8f, 0.8f, 1.0f);
        leaderboardFontParams.flip = false;
        leaderboardFontParams.incremental = true;
        leaderboardFontParams.minFilter = leaderboardFontParams.magFilter = Texture.TextureFilter.Linear;
        leaderboardFont = freetypeGeneratorArial.generateFont(leaderboardFontParams);

        FreeTypeFontGenerator.FreeTypeFontParameter usernameFontParams = new FreeTypeFontGenerator.FreeTypeFontParameter();
        usernameFontParams.characters += ArUtils.getAllChars().toString("");
        usernameFontParams.size = 16;
        usernameFontParams.color = new Color(0.8f, 0.8f, 0.8f, 1.0f);
        usernameFontParams.flip = false;
        usernameFontParams.incremental = true;
        usernameFontParams.minFilter = usernameFontParams.magFilter = Texture.TextureFilter.Linear;
        usernameFont = freetypeGeneratorArial.generateFont(usernameFontParams);

        FreeTypeFontGenerator.FreeTypeFontParameter timeFontParams = new FreeTypeFontGenerator.FreeTypeFontParameter();
        timeFontParams.characters += ArUtils.getAllChars().toString("");
        timeFontParams.size = 20 * Gdx.graphics.getWidth() / screenWidth;
        timeFontParams.color = new Color(1f, 1f, 1f, 1f);
        timeFontParams.flip = false;
        timeFontParams.incremental = true;
        timeFontParams.minFilter = timeFontParams.magFilter = Texture.TextureFilter.Linear;
        timeFont = freetypeGeneratorArial.generateFont(timeFontParams);
    }

    private void initTiles() {
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, 2 * SCREEN_WIDTH_LANDSCAPE, 2 * SCREEN_WIDTH_LANDSCAPE, false);
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
                screenX = (int) ((screenX - Gdx.graphics.getWidth() / 2f) * screenWidth / Gdx.graphics.getWidth());
                screenY = (int) ((Gdx.graphics.getHeight() / 2f - screenY) * screenHeight / Gdx.graphics.getHeight());
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
                screenX = (int) ((screenX - Gdx.graphics.getWidth() / 2f) * controllerCamera.viewportWidth / Gdx.graphics.getWidth());
                screenY = (int) ((Gdx.graphics.getHeight() / 2f - screenY) * controllerCamera.viewportHeight / Gdx.graphics.getHeight());
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
                        usernameFont.setColor(ColorUtil.bc_color_index_to_rgba[player.color - 1]);
                        usernameFont.draw(batch, player._name, x, y);
                    }
                }
            }
        }
    }

    private void drawProgressbar(ColorMeta colorMeta, String name, float dt, boolean drawStatic) {
        DecimalFormat decimalFormat = new DecimalFormat("#0.00");
        float totalWidth = progressbarWidth - progressbarInitWidth;
        float percentage = colorMeta.numCells / (float) TOTAL_CELLS;
        colorMeta._percentage = MathUtils.lerp(colorMeta._percentage, percentage, 0.04f);
        float width = colorMeta._percentage * totalWidth + progressbarInitWidth;
        colorMeta.progressBar.setSize(progressbarWidth, progressbarHeight);

        colorMeta.progressBar.setX(guiCamera.viewportWidth / 2f - width);

        float y;
        if (drawStatic && colorMeta._position > (LEADERBOARD_NUM + 1)) {
            y = guiCamera.viewportHeight / 2f - progressbarTopMargin - LEADERBOARD_NUM * (progressbarHeight + progressbarGap) - progressbarHeight - progressbarExtraGapForCurrentPlayer;
        } else {
            y = guiCamera.viewportHeight / 2f - progressbarTopMargin - (colorMeta._position - 1) * (progressbarHeight + progressbarGap) - progressbarHeight;
        }


        if (drawStatic || !colorMeta.positionIsChanging) {
            colorMeta.progressBar.setY(y);
        } else {
            if (colorMeta.progressBar.getY() < y) {
                colorMeta.progressBar.translateY(dt * LEADERBORAD_CHANGE_SPEED);
                if (colorMeta.progressBar.getY() > y) {
                    colorMeta.progressBar.setY(y);
                    colorMeta.positionIsChanging = false;
                }
            } else {
                colorMeta.progressBar.translateY(-dt * LEADERBORAD_CHANGE_SPEED);
                if (colorMeta.progressBar.getY() < y) {
                    colorMeta.progressBar.setY(y);
                    colorMeta.positionIsChanging = false;
                }
            }
        }
        colorMeta.progressBar.draw(batch);
        leaderboardFont.setColor(ColorUtil.bc_color_index_to_rgba[colorMeta.color - 1]);
        leaderboardFont.draw(batch, colorMeta._position + "- " + decimalFormat.format(percentage * 100f) + "% " + name, colorMeta.progressBar.getX() + 6 * guiUnits, colorMeta.progressBar.getY() + (progressbarHeight + leaderboardFont.getLineHeight()) / 2f - 2 * guiUnits);
    }

    private void drawLeaderboard(float dt) {
        synchronized (colorMetas) {

            Collections.sort(colorMetas, colorMetaComp);

            for (int i = 0; i < colorMetas.size() - 1; i++) {
                ColorMeta colorMeta = colorMetas.get(i);
                if (colorMeta.positionIsChanging) break;
                if (colorMeta.position > colorMeta._position) { // yani bayad bere paeen
                    ColorMeta next = colorMetas.get(i + 1);
                    if (!next.positionIsChanging && next.position <= next._position) {
                        if (colorMeta._position <= LEADERBOARD_NUM) {
                            if (next._position <= LEADERBOARD_NUM + 1) {
                                colorMeta.positionIsChanging = true;
                                colorMeta.changeDir = ColorMeta.CHANGE_DIRECTION_DOWN;
                                next.positionIsChanging = true;
                                next.changeDir = ColorMeta.CHANGE_DIRECTION_UP;
                            }
                        }
                        colorMeta._position++;
                        next._position--;
                        i++;
                    }
                }
            }

            Collections.sort(colorMetas, colorMetaComp);

            boolean playerProgressPrinted = false;
            Player currentPlayer = room.getState().players.get(client.getId());
            for (int i = 0; i < (LEADERBOARD_NUM + 1); i++) {
                ColorMeta colorMeta = colorMetas.get(i); // _position = i + 1
                if (colorMeta == null) continue;
                if (i == LEADERBOARD_NUM) {
                    if (!colorMeta.positionIsChanging) break;
                    if (colorMeta.changeDir == ColorMeta.CHANGE_DIRECTION_UP) {
//                        colorMeta.positionIsChanging = false;
                        break;
                    }
                }
                Player player = playersByColor[colorMeta.color - 1];
                if (player == null) continue;
                drawProgressbar(colorMeta, player._name, dt, false);
                if (currentPlayer != null && currentPlayer.color == colorMeta.color)
                    playerProgressPrinted = true;
            }

            if (!playerProgressPrinted) {
                if (currentPlayer == null) return;
                ColorMeta colorMeta = room.getState().colorMeta.get(currentPlayer.color + "");
                if (colorMeta == null) return;
                drawProgressbar(colorMeta, currentPlayer._name, dt, true);
            }

        }

    }

    private void drawTime() {
        long remainingTime = room.getState().endTime - getServerTime();
        int seconds = (int) (remainingTime / 1000) % 60;
        String secondsText = (seconds < 10 ? "0" : "") + seconds;
        int minutes = (int) (remainingTime / 60000);
        String minutesText = (minutes < 10 ? "0" : "") + minutes;
        float x = -Gdx.graphics.getWidth() / 2f + 10 * guiUnits;
        float y = Gdx.graphics.getHeight() / 2f - 10 * guiUnits;
        timeBg.setSize(timeText.width + 12 * guiUnits, timeText.height + 12 * guiUnits);
        timeBg.setPosition(x - 6 * guiUnits, y - 6 * guiUnits - timeText.height);
        timeBg.draw(batch);
        timeFont.draw(batch, minutesText + ":" + secondsText, x, y);
    }

    private void drawYouWillRespawnText() {
        Player player = room.getState().players.get(client.getId());
        if (player == null || player.status != 1) return;
        int remainingTime = (int) (player.rspwnTime - getServerTime());
        int seconds = remainingTime / 1000;
        if (seconds > 9) return;
        float x = -youWillRspwnText.width / 2f;
        float y = Gdx.graphics.getHeight() / 4f;
        youWillRspwnBg.setSize(youWillRspwnText.width + 12 * guiUnits, youWillRspwnText.height + 12 * guiUnits);
        youWillRspwnBg.setPosition(x - 6 * guiUnits, y - youWillRspwnText.height - 6 * guiUnits);
        youWillRspwnBg.draw(batch);
        timeFont.draw(batch, "You will respawn in " + seconds + " seconds", x, y);
    }

    private void clearPlayerPath(String clientId) {
        Player player = room.getState().players.get(clientId);
        if (player != null && player.trailGraphic != null) {
            Gdx.app.postRunnable(() -> player.trailGraphic.truncateAt(0));
            synchronized (player.pathCells) {
                player.pathCells.clear();
            }
//            if (ADD_FAKE_PATH_CELLS) {
//                player.pathCellUpdates.clear();
//                synchronized (pathCellGrid) {
//                    for (int i = 0; i < CELL_GRID_WIDTH; i++) {
//                        for (int j = 0; j < CELL_GRID_HEIGHT; j++) {
//                            if (pathCellGrid[i] != null && pathCellGrid[i][j] != null && pathCellGrid[i][j].color == player.color) {
//                                pathCellGrid[i][j] = null;
//                            }
//                        }
//                    }
//                }
//            }
        }
    }

    /* ***************************************** LOGIC *******************************************/

    private void updatePlayersPositions(float dt) {
        synchronized (players) {
            for (Player player : players) {
                if (player.bc != null && player.status == 0) {

//                    if (ADD_FAKE_PATH_CELLS) {
//                        synchronized (player.pathCells) {
//                            while (true) {
//                                PathCellUpdate update = player.pathCellUpdates.peek();
//                                if (update == null) break;
//                                if (update.time > (System.currentTimeMillis() - PATH_CELLS_UPDATE_TIME))
//                                    break;
//                                synchronized (pathCellGrid) {
//                                    Cell cell;
//                                    if ((cell = player.pathCells.get(update.key)) != null) {
//                                        if (pathCellGrid[cell.x + CELL_GRID_WIDTH / 2] != null && pathCellGrid[cell.x + CELL_GRID_WIDTH / 2][cell.y + CELL_GRID_HEIGHT / 2] != null) {
//                                            pathCellGrid[cell.x + CELL_GRID_WIDTH / 2][cell.y + CELL_GRID_HEIGHT / 2] = null;
//                                        }
//
//                                    }
//                                    if (pathCellGrid[update.cell.x + CELL_GRID_WIDTH / 2] == null)
//                                        pathCellGrid[update.cell.x + CELL_GRID_WIDTH / 2] = new Cell[CELL_GRID_HEIGHT];
//                                    pathCellGrid[update.cell.x + CELL_GRID_WIDTH / 2][update.cell.y + CELL_GRID_HEIGHT / 2] = update.cell;
//                                }
//                                player.pathCells.put(update.key, update.cell);
//                                player.pathCellUpdates.pop();
//                            }
//                        }
//                    }

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

//                    if (ADD_FAKE_PATH_CELLS) {
//                        if (room.getState().started && !room.getState().ended)
//                            processPlayerPosition(player, x, y);
//                    }
                }
            }
        }
    }

//    private void processPlayerPosition(Player player, float x, float y) {
//        int yi = (int) Math.floor((y + GRID_HEIGHT / 2f) / GRID_HEIGHT);
//        int xi = (int) (yi % 2 == 0 ? Math.floor((x + GRID_WIDTH / 2f) / GRID_WIDTH) : Math.floor(x / GRID_WIDTH));
//
////        System.out.println("player " + player.clientId + " is at " + xi + ", " + yi);
//
//        if (cellGrid[xi + CELL_GRID_WIDTH / 2] == null || cellGrid[xi + CELL_GRID_WIDTH / 2][yi + CELL_GRID_HEIGHT / 2] == null || cellGrid[xi + CELL_GRID_WIDTH / 2][yi + CELL_GRID_HEIGHT / 2].color != player.color) {
//            synchronized (pathCellGrid) {
//                if (pathCellGrid[xi + CELL_GRID_WIDTH / 2] == null || pathCellGrid[xi + CELL_GRID_WIDTH / 2][yi + CELL_GRID_HEIGHT / 2] == null) {
//                    Cell cell = new Cell();
//                    //cell.owner = player.clientId;
//                    cell.color = player.color;
//                    cell.x = (short) xi;
//                    cell.y = (short) yi;
//                    cell.id = gameAtlas.createSprite(TEXTURE_REGION_HEX_WHITE);
//                    cell.id.setSize(40, 46);
//                    Vector2 pos = getHexPosition(cell.x, cell.y);
//                    cell.id.setCenter(pos.x, pos.y);
//                    Color color = ColorUtil.bc_color_index_to_rgba[cell.color - 1];
//                    cell.id.setColor((1 - PATH_CELL_ALPHA_TINT) + PATH_CELL_ALPHA_TINT * color.r, (1 - PATH_CELL_ALPHA_TINT) + PATH_CELL_ALPHA_TINT * color.g, (1 - PATH_CELL_ALPHA_TINT) + PATH_CELL_ALPHA_TINT * color.b, 1.0f);
//
//                    if (pathCellGrid[xi + CELL_GRID_WIDTH / 2] == null)
//                        pathCellGrid[xi + CELL_GRID_WIDTH / 2] = new Cell[CELL_GRID_HEIGHT];
//                    pathCellGrid[xi + CELL_GRID_WIDTH / 2][yi + CELL_GRID_HEIGHT / 2] = cell;
//                    synchronized (player.pathCells) {
//                        player.pathCells.put(player.pathCells.size(), cell);
//                    }
//                } /*else if (!pathCellGrid[xi + CELL_GRID_WIDTH / 2][yi + CELL_GRID_HEIGHT / 2].owner.equals(player.clientId)) {
//                    // path cell male yeki dgas
//                }*/
//            }
//        } else {
//            // in home
//            clearPlayerPath(player.clientId);
//        }
//    }

    private void updateZoom() {
        Player player = room.getState().players.get(client.getId());
        if (player == null) return;
        ColorMeta cm = room.getState().colorMeta.get(player.color + "");
        if (cm == null) return;
//        float percentage = cm.numCells / (float) TOTAL_CELLS;
//        System.out.println("percentage = " + percentage);
//        camera.zoom = Math.min(CAMERA_INIT_ZOOM + 2f * percentage, 1.7f);
        camera.zoom = Math.min(CAMERA_INIT_ZOOM + cm.numCells * 0.001f, 1.7f);

    }

    // TODO: we need to improve this a little bit
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
            d = 10;
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
            float dx = screenX;
            float dy = -screenY;
            direction = (int) Math.toDegrees(Math.atan2(-dy, dx));
        } else if (controllerType == CONTROLLER_TYPE_PAD && mouseIsDown) {
            thumbstickBgSprite.setCenter(padAnchorPoint.x, padAnchorPoint.y);
            padVector.set(screenX - padAnchorPoint.x, screenY - padAnchorPoint.y);
            if (padVector.len2() > PAD_CONTROLLER_MAX_LENGTH * PAD_CONTROLLER_MAX_LENGTH) {
                padVector.nor().scl(PAD_CONTROLLER_MAX_LENGTH);
            }
            thumbstickPadSprite.setCenter(padAnchorPoint.x + padVector.x, padAnchorPoint.y + padVector.y);
            direction = (int) Math.toDegrees(Math.atan2(padVector.y, padVector.x));
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
            direction = (int) Math.toDegrees(Math.atan2(padVector.y, padVector.x));
        }
    }

    private void updateGuiValues() {
        float lessValue = (guiCamera.viewportWidth < guiCamera.viewportHeight) ? guiCamera.viewportWidth : guiCamera.viewportHeight;

        progressbarWidth = lessValue * 0.6f;
        progressbarInitWidth = lessValue * 0.25f;
        guiUnits = lessValue * 0.002f;

        progressbarGap = 1;//lessValue / 400f;
        progressbarHeight = lessValue / 18f;
        progressbarTopMargin = lessValue / 125f;
        progressbarExtraGapForCurrentPlayer = progressbarHeight + lessValue / 100f;
    }

    /* **************************************** NETWORK ******************************************/

    private void connectToServer() {
        client = new Client(ENDPOINT, ConfigFile.get("clientId"), null, null, 10000, new Client.Listener() {
            @Override
            public void onOpen(String id) {
                ConfigFile.set("clientId", id);
                LinkedHashMap<String, Object> options = new LinkedHashMap<>();
                options.put("name", "میلاد");
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
                            if (player.color == 0) return;
                            synchronized (players) {
                                players.add(player);
                            }
                            playersByColor[player.color - 1] = player;

                            player._name = arFont.getText(player.name);

                            Color bcColor = ColorUtil.bc_color_index_to_rgba[player.color - 1];
                            Color cColor = ColorUtil.c_color_index_to_rgba[player.color - 1];

                            Gdx.app.postRunnable(() -> {
                                player.text = new GlyphLayout(usernameFont, player._name);

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

                                player.trailGraphic = new TrailGraphic();
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
                            playersByColor[player.color - 1] = null;
                        };

                        room.getState().cells.onAddListener = (cell, key) -> {
                            synchronized (cells) {
                                cells.put(key, cell);
                            }
//                            if (cellGrid[cell.x + CELL_GRID_WIDTH / 2] == null)
//                                cellGrid[cell.x + CELL_GRID_WIDTH / 2] = new Cell[CELL_GRID_HEIGHT];
//                            cellGrid[cell.x + CELL_GRID_WIDTH / 2][cell.y + CELL_GRID_HEIGHT / 2] = cell;

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
//                            if (cellGrid[cell.x + CELL_GRID_WIDTH / 2] != null) {
//                                cellGrid[cell.x + CELL_GRID_WIDTH / 2][cell.y + CELL_GRID_HEIGHT / 2] = null;
//                            }
                        };

                        room.getState().colorMeta.onAddListener = (colorMeta, key) -> {
                            colorMeta._position = colorMetas.size() + 1;
                            colorMeta._percentage = colorMeta.numCells / (float) TOTAL_CELLS;
                            colorMeta.progressBar = gameAtlas.createSprite(TEXTURE_REGION_PROGRESSBAR);
                            colorMeta.progressBar.setColor(ColorUtil.c_color_index_to_rgba[Integer.parseInt(key) - 1]);
                            colorMeta.progressBar.setX(Gdx.graphics.getWidth() / 2f - (colorMeta._percentage * (progressbarWidth - progressbarInitWidth) + progressbarInitWidth));
                            colorMeta.progressBar.setY(Gdx.graphics.getHeight() / 2f - progressbarTopMargin - Math.min(colorMeta._position - 1, LEADERBOARD_NUM) * (progressbarHeight + progressbarGap) - progressbarHeight);
                            synchronized (colorMetas) {
                                colorMetas.add(colorMeta);
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