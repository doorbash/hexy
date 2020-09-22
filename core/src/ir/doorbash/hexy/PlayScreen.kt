package ir.doorbash.hexy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
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
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Timer;
import com.crowni.gdx.rtllang.support.ArFont;
import com.crowni.gdx.rtllang.support.ArUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

import gnu.trove.impl.sync.TSynchronizedLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import io.colyseus.Client;
import io.colyseus.Client.MatchMakeException;
import io.colyseus.Room;
import io.colyseus.serializer.schema.Schema;
import ir.doorbash.hexy.model.Cell;
import ir.doorbash.hexy.model.Item;
import ir.doorbash.hexy.model.MyState;
import ir.doorbash.hexy.model.Player;
import ir.doorbash.hexy.model.Point;
import ir.doorbash.hexy.util.ColorUtil;
import ir.doorbash.hexy.util.Constants;
import ir.doorbash.hexy.util.FontDrawItem;
import ir.doorbash.hexy.util.I18N;
import ir.doorbash.hexy.util.TextUtil;
import ir.doorbash.hexy.util._Math;

/**
 * Created by Milad Doorbash on 7/22/2019.
 */
public class PlayScreen extends ScreenAdapter {

    /* *************************************** CONSTANTS *****************************************/

    private static final boolean DEBUG_SHOW_GHOST = false;
    private static final boolean DEBUG_DRAW_PIDS = false;
    private static final boolean CORRECT_PLAYER_POSITION = true;
    private static final boolean ADD_FAKE_PATH_CELLS = false;
    private static final boolean ROTATE_CAMERA = false;

    private static final int MAP_SIZE = 50;
    private static final int EXTENDED_CELLS = 4;
    private static final int TOTAL_CELLS = (2 * MAP_SIZE + 1) * (2 * MAP_SIZE + 1);
    private static final int CORRECT_PLAYER_POSITION_INTERVAL = 100;
    private static final int SEND_DIRECTION_INTERVAL = 200;
    private static final int CHECK_CONNECTION_INTERVAL = 4000;
    private static final int SEND_PING_INTERVAL = 5000;
    private static final int PAD_CONTROLLER_MAX_LENGTH = 42;
    private static final int LEADERBOARD_NUM = 4;
    private static final int SCREEN_WIDTH_PORTRAIT = 480;
    private static final int SCREEN_WIDTH_LANDSCAPE = 800;

    //    private static final int PATH_CELLS_UPDATE_TIME = 500;
    private static final int GAME_MODE_FFA = 0;
    private static final int GAME_MODE_BATTLE = 1;

    private static final int CONNECTION_STATE_DISCONNECTED = 0;
    private static final int CONNECTION_STATE_CONNECTING = 1;
    private static final int CONNECTION_STATE_CONNECTED = 2;
    private static final int CONNECTION_STATE_CLOSED = 3;

    private static final float CAMERA_LERP = 0.8f;
    private static final float CAMERA_DEATH_LERP = 0.4f;
    private static final float CAMERA_INIT_ZOOM = 0.7f;
    private static final float ON_SCREEN_PAD_RELEASE_TOTAL_TIME = 0.3f;
    private static final float GRID_WIDTH = 44;
    private static final float GRID_HEIGHT = 38;
    //    private static final float MAP_SIZE_X_PIXEL = (MAP_SIZE * GRID_WIDTH);
    private static final float MAP_SIZE_X_EXT_PIXEL = (MAP_SIZE + EXTENDED_CELLS) * GRID_WIDTH;
    //    private static final float MAP_SIZE_Y_PIXEL = (MAP_SIZE * GRID_HEIGHT);
    private static final float MAP_SIZE_Y_EXT_PIXEL = (MAP_SIZE + EXTENDED_CELLS) * GRID_HEIGHT;
    private static final float LEADERBORAD_CHANGE_SPEED = 90;
    private static final float PLAYER_ROTATE_SPEED = 2;
    //    private static final float HIGH_LERP_TIME = 2; // seconds
    private static final float ROPE_WIDTH = 20;

    private static final float STROKE_SIZE = 46;
    private static final float FILL_SIZE = 36;
    private static final float INDIC_SIZE = 80;
    private static final float CELL_WIDTH = 40;
    private static final float CELL_TEX_WIDTH = 34;
    private static final float CELL_HEIGHT = 46;

//    private static final float BC_SIZE = 46 * 1.2f;
//    private static final float C_SIZE = 36 * 1.2f;
//    private static final float INDIC_SIZE = 80 * 1.2f;

    private static final String TAG = "PlayScreen";

    private static final String PATH_FONT_NOTO = "fonts/NotoSans-Regular.ttf";
    private static final String PATH_FONT_ARIAL = "fonts/arialbd.ttf";

    private static final String PATH_PACK_ATLAS = "gfx/pack.atlas";
    private static final String PATH_FILL_ATLAS = "gfx/fill.atlas";
    private static final String PATH_TRAIL_TEXTURE = "gfx/trail.png";
    private static final String PATH_LOADING_SPRITESHEET = "gfx/loading.png";

    private static final String PATH_SOUND_CAPTURE = "sfx/capture1.wav";
    //    private static final String PATH_SOUND_CAPTURE = "sfx/capture2.mp3";
    private static final String PATH_SOUND_CLICK = "sfx/click2.wav";
    private static final String PATH_SOUND_BOOST = "sfx/boost1.wav";
    private static final String PATH_SOUND_COIN = "sfx/coin1.wav";
    private static final String PATH_SOUND_HIT = "sfx/hit1.wav";
    private static final String PATH_SOUND_DEATH = "sfx/lose1.wav";

    private static final String TEXTURE_REGION_HEX_WHITE = "hex_white";
    private static final String TEXTURE_REGION_THUMBSTICK_BG = "thumbstick-background";
    private static final String TEXTURE_REGION_THUMBSTICK_PAD = "thumbstick-pad";
    private static final String TEXTURE_REGION_BC = "bc";
    private static final String TEXTURE_REGION_INDIC = "indic";
    private static final String TEXTURE_REGION_PROGRESSBAR = "progressbar";
    private static final String TEXTURE_REGION_COIN = "coin";
    private static final String TEXTURE_REGION_BOOST = "boost";

    private static final Interpolation ON_SCREEN_PAD_RELEASE_ELASTIC_OUT = new Interpolation.ElasticOut(3, 2, 3, 0.5f);
    private static final Color COLOR_TIME_TEXT_BACKGROUND = new Color(0x707070cc);
    //    private static final Color COLOR_KILLS_TEXT_BACKGROUND = new Color(0x707070aa);
    private static final Color COLOR_KILLS_TEXT_BACKGROUND = new Color(0x606060aa);
    private static final Color COLOR_YOUR_BEST_PROGRESS_TEXT = new Color(0x707070cc);
    private static final Color COLOR_YOUR_PROGRESS_BG = new Color(0x70707088);
    private static final Color CONNECTING_TEXT_COLOR = Color.valueOf("#212121ff");
    private static final Color COLOR_BLOCKS_TEXT_FADE_OUT = Color.valueOf("#212121ff");
    private static final Color COLOR_COINS_TEXT_FADE_OUT = new Color(0x8F6F31ff/*0xa67c00ff*/);
    //    private static final Color COLOR_COINS_TEXT_FADE_OUT = Color.valueOf("#212121ff");
    private static final Comparator<Player> SORT_PLAYERS_BY_NUM_CELLS = (o1, o2) -> Integer.compare(o2.numCells, o1.numCells);
    private static final Comparator<Player> SORT_PLAYERS_BY_POSITION = (o1, o2) -> Integer.compare(o1._position, o2._position);

    private SpriteBatch batch;
    private OrthographicCamera gameCamera;
    private OrthographicCamera fixedCamera;
    private OrthographicCamera guiCamera;
    private Texture trailTexture;
    private TextureAtlas mainAtlas;
    private TextureAtlas fillAtlas;
    private TextureAtlas.AtlasRegion whiteHex;
    private FrameBuffer fbo;
    private Sprite tiles;
    private Sprite thumbstickBgSprite;
    private Sprite thumbstickPadSprite;
    private Sprite timeBg;
    private Sprite youWillRspwnBg;
    private Sprite playerProgressBar;
    private Sprite playerProgressBarBest;
    private Sprite statsBg;
    private Sprite coin;
    private FreeTypeFontGenerator freetypeGeneratorNoto;
    private FreeTypeFontGenerator freetypeGeneratorArial;
    private BitmapFont logFont;
    private BitmapFont usernameFont;
    private BitmapFont leaderboardFont;
    private BitmapFont statsFont;
    private BitmapFont timeFont;
    private GlyphLayout timeText;
    private GlyphLayout youWillRspwnText;
    private GlyphLayout yourProgressText;
    private GlyphLayout yourProgressBestText;
    private LoadingAnimation loadingAnimation;
    private Sound captureSound;
    private Sound clickSound;
    private Sound hitSound;
    private Sound boostSound;
    private Sound coinSound;
    private Sound deathSound;

    /* **************************************** FIELDS *******************************************/

    private boolean leaderboardDrawAgain;
    private boolean mouseIsDown = false;
    private boolean isUpdating = false;
    private boolean soundIsOn;
    private boolean graphicsHigh;
    private boolean deviceRotationAvailable;
//    private boolean controllerConnected;

    private int correctPlayerPositionTime = CORRECT_PLAYER_POSITION_INTERVAL;
    private int sendDirectionTime = SEND_DIRECTION_INTERVAL;
    private int sendPingTime = SEND_PING_INTERVAL;
    private int checkConnectionTime = -1;
    private int controllerType = Constants.CONTROL_FLOATING;
    private int screenWidth;
    private int screenHeight;
    private int currentPing;
    private int gameMode = GAME_MODE_FFA;
    private int connectionState = CONNECTION_STATE_DISCONNECTED;
    private int langCode;
    private int bottomYi;
    private int leftXi;
    private int sizeX;
    private int sizeY;
    private int coinValue;

    private long lastPingSentTime;
    private long lastPingReplyTime;
    private long timeDiff;

    private float lastDirection = -1000;
    private float direction = -1000;
    private float onScreenPadCurrentLen = 0;
    private float onScreenPadReleaseTimer = 0;
    private float onScreenPadInitLen = 0;
    private float progressbarWidth;
    private float progressbarHeight;
    private float progressbarTopMargin;
    private float progressbarGap;
    private float progressbarInitWidth;
    private float yourProgressbarInitWidth;
    private float progressbarExtraGapForCurrentPlayer;
    private float guiUnits;
    private float yourProgressbarWidth;
    private float playerBestProgress = 0.448f;
    private float time;
    private float actualWidth;
    private float actualHeight;
    private float leftX;
    private float bottomY;
    private float leaderboardChangeSpeed;

    private String sessionId;
    private String roomId;

    private float[] controllerAxis = new float[2];
    private float[] rotationMatrix = new float[4 * 4];
    private final LinkedHashMap<String, Object> message = new LinkedHashMap<>();
    private final List<Player> leaderboardList = new ArrayList<>();
    private static final Object playersMutex = new Object();
    private TSynchronizedLongObjectMap<Player> players = new TSynchronizedLongObjectMap<>(new TLongObjectHashMap<>(), playersMutex);
    private List<Long> drawList = new ArrayList<>();
    private List<FontDrawItem> leaderboardDrawList = new ArrayList<>();
    private final Cell[][] cells = new Cell[2 * MAP_SIZE + 1][2 * MAP_SIZE + 1];
    private final Cell[][] pathCells = new Cell[2 * MAP_SIZE + 1][2 * MAP_SIZE + 1];
    private final List<TextFadeOutAnimation> textFadeOutAnimations = new ArrayList<>();

    private final Vector2 padAnchorPoint = new Vector2();
    private final Vector2 padVector = new Vector2();
    private final Vector2 onScreenPadPosition = new Vector2();
    private Vector2 onScreenPadNorVector = new Vector2();
    private final Vector2 deathPosition = new Vector2();

    //    private Client client;
    private Room<MyState> room;
    private final ArFont arFont = new ArFont();
    private Player currentPlayer;
    private Preferences prefs;

    /* ************************************** CONSTRUCTOR ****************************************/

    PlayScreen() {
        prefs = Gdx.app.getPreferences(Constants.PREFS_NAME);
        langCode = I18N.getLangCode(prefs.getString(Constants.KEY_SETTINGS_LANGUAGE, Constants.DEFAULT_SETTINGS_LANGUAGE));
        soundIsOn = prefs.getBoolean(Constants.KEY_SETTINGS_SOUND, true);
        graphicsHigh = prefs.getString(Constants.KEY_SETTINGS_GRAPHICS, Constants.DEFAULT_SETTINGS_GRAPHICS).equals("high");
        controllerType = prefs.getInteger(Constants.KEY_SETTINGS_CONTROL, Constants.DEFAULT_SETTINGS_CONTROL);
        coinValue = prefs.getInteger(Constants.KEY_COINS, 0);
        boolean isRotationVectorAvailable = Gdx.input.isPeripheralAvailable(Input.Peripheral.RotationVector);
        boolean isAccelerometerAvailable = Gdx.input.isPeripheralAvailable(Input.Peripheral.Accelerometer);
        boolean isCompassAvailable = Gdx.input.isPeripheralAvailable(Input.Peripheral.Compass);
        deviceRotationAvailable = isRotationVectorAvailable || (isAccelerometerAvailable && isCompassAvailable);
        if (controllerType == Constants.CONTROL_DEVICE_ROTATION && !deviceRotationAvailable) {
            controllerType = Constants.CONTROL_TOUCH;
        }

        batch = new SpriteBatch();

        mainAtlas = new TextureAtlas(PATH_PACK_ATLAS);
        if (graphicsHigh) fillAtlas = new TextureAtlas(PATH_FILL_ATLAS);
        whiteHex = mainAtlas.findRegion(TEXTURE_REGION_HEX_WHITE);
        thumbstickBgSprite = mainAtlas.createSprite(TEXTURE_REGION_THUMBSTICK_BG);
        thumbstickBgSprite.setSize(152, 152);
        thumbstickPadSprite = mainAtlas.createSprite(TEXTURE_REGION_THUMBSTICK_PAD);
        thumbstickPadSprite.setSize(70, 70);
        timeBg = mainAtlas.createSprite(TEXTURE_REGION_PROGRESSBAR);
        statsBg = mainAtlas.createSprite(TEXTURE_REGION_PROGRESSBAR);
        youWillRspwnBg = mainAtlas.createSprite(TEXTURE_REGION_PROGRESSBAR);
        youWillRspwnBg.setColor(COLOR_TIME_TEXT_BACKGROUND);
        timeBg.setColor(COLOR_TIME_TEXT_BACKGROUND);
        statsBg.setColor(COLOR_KILLS_TEXT_BACKGROUND);
        playerProgressBar = mainAtlas.createSprite(TEXTURE_REGION_PROGRESSBAR);
        playerProgressBarBest = mainAtlas.createSprite(TEXTURE_REGION_PROGRESSBAR);
        playerProgressBarBest.setColor(COLOR_YOUR_PROGRESS_BG);
        loadingAnimation = new LoadingAnimation(PATH_LOADING_SPRITESHEET);
        coin = mainAtlas.createSprite(TEXTURE_REGION_COIN);
        gameCamera = new OrthographicCamera();
        gameCamera.zoom = CAMERA_INIT_ZOOM;
        fixedCamera = new OrthographicCamera();
        guiCamera = new OrthographicCamera();

        if (soundIsOn) {
            boostSound = Gdx.audio.newSound(Gdx.files.internal(PATH_SOUND_BOOST));
            coinSound = Gdx.audio.newSound(Gdx.files.internal(PATH_SOUND_COIN));
            clickSound = Gdx.audio.newSound(Gdx.files.internal(PATH_SOUND_CLICK));
            deathSound = Gdx.audio.newSound(Gdx.files.internal(PATH_SOUND_DEATH));
            hitSound = Gdx.audio.newSound(Gdx.files.internal(PATH_SOUND_HIT));
            captureSound = Gdx.audio.newSound(Gdx.files.internal(PATH_SOUND_CAPTURE));
        }

        init(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        gameCamera.viewportWidth = screenWidth;
        gameCamera.viewportHeight = screenHeight;
        gameCamera.update();
        fixedCamera.viewportWidth = screenWidth;
        fixedCamera.viewportHeight = screenHeight;
        fixedCamera.update();
        guiCamera.viewportWidth = Gdx.graphics.getWidth();
        guiCamera.viewportHeight = Gdx.graphics.getHeight();
        guiCamera.update();
        updateGuiValues();

        trailTexture = new Texture(Gdx.files.internal(PATH_TRAIL_TEXTURE), true);
        trailTexture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        trailTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        initFonts();

        timeText = new GlyphLayout(timeFont, "99:99");
        youWillRspwnText = new GlyphLayout(timeFont, I18N.texts[langCode][I18N.you_will_respawn_in_9_seconds]); // TODO: handle rtl :/
        yourProgressText = new GlyphLayout(leaderboardFont, "99.99%");
        yourProgressBestText = new GlyphLayout(leaderboardFont, "BEST 99.99%");

        initTiles();

        initInput();

        initControllers();
    }

    /* *************************************** OVERRIDE *****************************************/


    @Override
    public void render(float dt) {
        time += dt;

        //Gdx.gl.glClearColor(0f, 0f, 0f, 1);
        //Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
//        Gdx.gl20.glEnable(GL20.GL_BLEND);
//        Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        if (room != null) {
            currentPlayer = room.state.players.get(room.getSessionId());
            if (connectionState < CONNECTION_STATE_CLOSED) {
                if (currentPlayer != null && currentPlayer._stroke != null) {
//                camera.position.x = player.bc.getX() + player.bc.getWidth() / 2f;
//                camera.position.y = player.bc.getY() + player.bc.getHeight() / 2f;
                    gameCamera.position.x = MathUtils.lerp(gameCamera.position.x, currentPlayer._stroke.getX() + currentPlayer._stroke.getWidth() / 2f, CAMERA_LERP);
                    gameCamera.position.y = MathUtils.lerp(gameCamera.position.y, currentPlayer._stroke.getY() + currentPlayer._stroke.getHeight() / 2f, CAMERA_LERP);
                    if (ROTATE_CAMERA) {
                        float camAngle = -_Math.getCameraCurrentXYAngle(gameCamera);
                        float playerAngle = currentPlayer._angle * MathUtils.radiansToDegrees - 90;
                        while (playerAngle < 0) playerAngle += 360;
                        while (playerAngle >= 360) playerAngle -= 360;
                        gameCamera.rotate(camAngle - playerAngle);
                    }
                }
            } else {
                if (currentPlayer != null && currentPlayer._stroke != null) {
                    gameCamera.position.x = MathUtils.lerp(gameCamera.position.x, deathPosition.x, CAMERA_DEATH_LERP);
                    gameCamera.position.y = MathUtils.lerp(gameCamera.position.y, deathPosition.y, CAMERA_DEATH_LERP);
                }
            }
        }
        gameCamera.update();

        actualWidth = gameCamera.zoom * gameCamera.viewportWidth;
        actualHeight = gameCamera.zoom * gameCamera.viewportHeight;
        leftX = gameCamera.position.x - actualWidth / 2f;
        bottomY = gameCamera.position.y - actualHeight / 2f;
        bottomYi = (int) Math.floor((bottomY + GRID_HEIGHT / 2f) / GRID_HEIGHT) - 1;
        leftXi = (int) (bottomYi % 2 == 0 ? Math.floor((leftX + GRID_WIDTH / 2f) / GRID_WIDTH) : Math.floor(leftX / GRID_WIDTH)) - 1;
        sizeX = (int) (actualWidth / GRID_WIDTH) + 3;
        sizeY = (int) (actualHeight / GRID_HEIGHT) + 3;

        drawList.clear();

        batch.setProjectionMatrix(gameCamera.combined);
        batch.begin();
        batch.disableBlending();
        drawTiles();
        batch.enableBlending();
        batch.flush();

        if (room != null && ((connectionState == CONNECTION_STATE_CONNECTED && !isUpdating) || connectionState == CONNECTION_STATE_CLOSED)) {
            if (connectionState == CONNECTION_STATE_CONNECTED) {
                updatePlayersPositions(dt);
                updateZoom();
            }
            drawCells();
            if (graphicsHigh) drawTextureCells();
            batch.end();
            drawTrails();
            batch.begin();
            drawItems();
            drawPlayers();
            drawPlayerFillTextures();
            drawPlayerNames();
            drawTextFadeOutAnimations(dt);
        }

        batch.setProjectionMatrix(fixedCamera.combined);

        if ((controllerType == Constants.CONTROL_FIXED_LEFT || controllerType == Constants.CONTROL_FIXED_RIGHT) && !mouseIsDown && !MathUtils.isEqual(onScreenPadCurrentLen, 0)) {
//            System.out.println("bouncing....");
            onScreenPadReleaseTimer += dt;
            onScreenPadCurrentLen = (1 - ON_SCREEN_PAD_RELEASE_ELASTIC_OUT.apply(Math.min(1, onScreenPadReleaseTimer / ON_SCREEN_PAD_RELEASE_TOTAL_TIME))) * onScreenPadInitLen;
//            System.out.println("current pad length is " + onScreenPadCurrentLen);
            padVector.set(onScreenPadNorVector.x * onScreenPadCurrentLen, onScreenPadNorVector.y * onScreenPadCurrentLen);
            thumbstickPadSprite.setCenter(onScreenPadPosition.x + padVector.x, onScreenPadPosition.y + padVector.y);
        }

        if ((controllerType == Constants.CONTROL_FLOATING && mouseIsDown) || (controllerType == Constants.CONTROL_FIXED_LEFT || controllerType == Constants.CONTROL_FIXED_RIGHT)) {
            thumbstickBgSprite.draw(batch);
            thumbstickPadSprite.draw(batch);
        }

        batch.setProjectionMatrix(guiCamera.combined);

        if (connectionState == CONNECTION_STATE_CONNECTED && room != null && room.state.started) {
            leaderboardDrawList.clear();
            drawLeaderboard(dt);
            if (gameMode == GAME_MODE_BATTLE && !room.state.ended) {
                drawTime();
                drawYouWillRespawnText();
            } else if (gameMode == GAME_MODE_FFA) {
//                drawCoin();
                drawStatsBackground();
                drawPlayerProgress();
//                drawCoinText();
                drawStatsText();
            }
            for (FontDrawItem fdi : leaderboardDrawList) {
                leaderboardFont.setColor(fdi.color);
                leaderboardFont.draw(batch, fdi.text, fdi.x, fdi.y);
            }
        }

        if ((connectionState == CONNECTION_STATE_CONNECTED && isUpdating) || connectionState == CONNECTION_STATE_CONNECTING || connectionState == CONNECTION_STATE_DISCONNECTED) {
            drawConnecting(dt);
        } else {
//            drawKills();
            drawPing();
//            connectTime += dt;
        }

        batch.end();

        if (sendDirectionTime < 0) {
            sendDirection();
            sendDirectionTime = SEND_DIRECTION_INTERVAL;
        } else sendDirectionTime -= dt * 1000;

        if (sendPingTime < 0) {
            sendPing();
            sendPingTime = SEND_PING_INTERVAL;
        } else sendPingTime -= dt * 1000;

        if (checkConnectionTime < 0) {
            checkConnection();
            checkConnectionTime = CHECK_CONNECTION_INTERVAL;
        } else checkConnectionTime -= dt * 1000;

        if (controllerType == Constants.CONTROL_DEVICE_ROTATION && deviceRotationAvailable) {
            Gdx.input.getRotationMatrix(rotationMatrix);
            Matrix4 m = new Matrix4(rotationMatrix);
            Quaternion q = m.getRotation(new Quaternion());
            direction = (int) Math.toDegrees(Math.atan2(q.getPitch(), -q.getYaw()));
//            System.out.println("pitch = " + q.getPitch());
//            System.out.println("yaw = " + -q.getYaw());
//            System.out.println(";;;;;;;;;;;;;;;;");
        }

        // game controller
        if (/*controllerConnected && */(Math.abs(controllerAxis[0]) > 0.1f || Math.abs(controllerAxis[1]) > 0.1f)) {
            direction = (int) Math.toDegrees(Math.atan2(-controllerAxis[0], controllerAxis[1]));
        }

//        System.out.println(";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;");
    }

    @Override
    public void resize(int width, int height) {
        if (controllerType == Constants.CONTROL_DEVICE_ROTATION) {
            //super.resize(width, height);
            return;
        }

        System.out.println("resize(" + width + ", " + height + ")");
        init(width, height);

        gameCamera.viewportWidth = screenWidth;
        gameCamera.viewportHeight = screenHeight;
        gameCamera.update();

        fixedCamera.viewportWidth = screenWidth;
        fixedCamera.viewportHeight = screenHeight;
        fixedCamera.update();

        guiCamera.viewportWidth = width;
        guiCamera.viewportHeight = height;
        guiCamera.update();

        updateGuiValues();

        leaderboardDrawAgain = true;
    }

    @Override
    public void dispose() {
        if (room != null) room.leave();
        if (batch != null) batch.dispose();
        if (fbo != null) fbo.dispose();
        if (mainAtlas != null) mainAtlas.dispose();
        if (fillAtlas != null) fillAtlas.dispose();
        if (logFont != null) logFont.dispose();
        if (usernameFont != null) usernameFont.dispose();
        if (leaderboardFont != null) leaderboardFont.dispose();
        if (statsFont != null) statsFont.dispose();
        if (timeFont != null) timeFont.dispose();
        if (freetypeGeneratorNoto != null) freetypeGeneratorNoto.dispose();
        if (freetypeGeneratorArial != null) freetypeGeneratorArial.dispose();
        if (loadingAnimation != null) loadingAnimation.dispose();
        if (captureSound != null) captureSound.dispose();
        if (boostSound != null) boostSound.dispose();
        if (coinSound != null) coinSound.dispose();
        if (hitSound != null) hitSound.dispose();
        if (deathSound != null) deathSound.dispose();
        if (clickSound != null) clickSound.dispose();
    }

    /* ***************************************** INIT *******************************************/

    private void init(int width, int height) {
        screenWidth = height > width ? SCREEN_WIDTH_PORTRAIT : SCREEN_WIDTH_LANDSCAPE;
        screenHeight = screenWidth * height / width;
        leaderboardChangeSpeed = LEADERBORAD_CHANGE_SPEED * height / screenHeight;
        if (controllerType == Constants.CONTROL_FIXED_RIGHT) {
            onScreenPadPosition.set(screenWidth / 2f - 120, -screenHeight / 2f + 120);
        } else {
            onScreenPadPosition.set(-screenWidth / 2f + 120, -screenHeight / 2f + 120);
        }
        if (controllerType == Constants.CONTROL_FIXED_LEFT || controllerType == Constants.CONTROL_FIXED_RIGHT) {
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

        FreeTypeFontGenerator.FreeTypeFontParameter statsFontParams = new FreeTypeFontGenerator.FreeTypeFontParameter();
        statsFontParams.characters += ArUtils.getAllChars().toString("");
        statsFontParams.size = 12 * Gdx.graphics.getWidth() / screenWidth;
        statsFontParams.color = new Color(1f, 1f, 1f, 1.0f);
        statsFontParams.flip = false;
        statsFontParams.incremental = true;
        statsFontParams.minFilter = statsFontParams.magFilter = Texture.TextureFilter.Linear;
        statsFont = freetypeGeneratorArial.generateFont(statsFontParams);

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

        Gdx.gl.glClearColor(0.92f, 0.92f, 0.92f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Matrix4 m = new Matrix4();
        m.setToOrtho2D(0, 0, fbo.getWidth(), fbo.getHeight());

        batch.setProjectionMatrix(m);

        batch.begin();
//        mainBatch.disableBlending();

        for (int xi = -3; xi < 37; xi++) {
            for (int yi = -3; yi < 43; yi++) {
                batch.draw(whiteHex, xi * GRID_WIDTH + (yi % 2 == 0 ? 0 : GRID_WIDTH / 2f) - CELL_WIDTH / 2f, yi * GRID_HEIGHT - CELL_HEIGHT / 2f, CELL_WIDTH, CELL_HEIGHT);
            }
        }

//        mainBatch.enableBlending();
        batch.end();

        fbo.end();

        tiles = new Sprite(fbo.getColorBufferTexture());

        tiles.flip(false, true);
    }

    private void initInput() {
        Gdx.input.setInputProcessor(new InputProcessor() {

            boolean leftIsDown = false;
            boolean rightIsDown = false;
            boolean upIsDown = false;
            boolean downIsDown = false;

            private void updateAxis() {
                if (rightIsDown) {
                    controllerAxis[1] = 1;
                } else if (leftIsDown) {
                    controllerAxis[1] = -1;
                } else {
                    controllerAxis[1] = 0;
                }
                if (upIsDown) {
                    controllerAxis[0] = -1;
                } else if (downIsDown) {
                    controllerAxis[0] = 1;
                } else {
                    controllerAxis[0] = 0;
                }
            }

            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.BACK) {
                    if (room != null) room.leave();
                    Gdx.app.exit();
                    return true;
                } else if (keycode == Input.Keys.RIGHT) {
                    rightIsDown = true;
                    updateAxis();
                    return true;
                } else if (keycode == Input.Keys.LEFT) {
                    leftIsDown = true;
                    updateAxis();
                    return true;
                } else if (keycode == Input.Keys.UP) {
                    upIsDown = true;
                    updateAxis();
                    return true;
                } else if (keycode == Input.Keys.DOWN) {
                    downIsDown = true;
                    updateAxis();
                    return true;
                }
                return false;
            }

            @Override
            public boolean keyUp(int keycode) {
                if (keycode == Input.Keys.RIGHT) {
                    rightIsDown = false;
                    updateAxis();
                    return true;
                } else if (keycode == Input.Keys.LEFT) {
                    leftIsDown = false;
                    updateAxis();
                    return true;
                } else if (keycode == Input.Keys.UP) {
                    upIsDown = false;
                    updateAxis();
                    return true;
                } else if (keycode == Input.Keys.DOWN) {
                    downIsDown = false;
                    updateAxis();
                    return true;
                }
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
                screenX = (int) ((screenX - Gdx.graphics.getWidth() / 2f) * fixedCamera.viewportWidth / Gdx.graphics.getWidth());
                screenY = (int) ((Gdx.graphics.getHeight() / 2f - screenY) * fixedCamera.viewportHeight / Gdx.graphics.getHeight());
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

    private void initControllers() {
        System.out.println("Controllers: " + Controllers.getControllers().size);
        int i = 0;
        for (Controller controller : Controllers.getControllers()) {
            System.out.println("#" + i++ + ": " + controller.getName());
        }
        if (Controllers.getControllers().size == 0) System.out.println("No controllers attached");
//        else controllerConnected = true;

        // setup the listener that prints events to the console
        Controllers.addListener(new ControllerListener() {
            public int indexOf(Controller controller) {
                return Controllers.getControllers().indexOf(controller, true);
            }

            @Override
            public void connected(Controller controller) {
                System.out.println("connected " + controller.getName());
                int i = 0;
                for (Controller c : Controllers.getControllers()) {
                    System.out.println("#" + i++ + ": " + c.getName());
                }
            }

            @Override
            public void disconnected(Controller controller) {
                System.out.println("disconnected " + controller.getName());
                int i = 0;
                for (Controller c : Controllers.getControllers()) {
                    System.out.println("#" + i++ + ": " + c.getName());
                }
                if (Controllers.getControllers().size == 0)
                    System.out.println("No controllers attached");
            }

            @Override
            public boolean buttonDown(Controller controller, int buttonIndex) {
                System.out.println("#" + indexOf(controller) + ", button " + buttonIndex + " down");
                return false;
            }

            @Override
            public boolean buttonUp(Controller controller, int buttonIndex) {
                System.out.println("#" + indexOf(controller) + ", button " + buttonIndex + " up");
                return false;
            }

            @Override
            public boolean axisMoved(Controller controller, int axisIndex, float value) {
                System.out.println("#" + indexOf(controller) + ", axis " + axisIndex + ": " + value);
                if (axisIndex == 0) controllerAxis[0] = value;
                else if (axisIndex == 1) controllerAxis[1] = value;
                return false;
            }

            @Override
            public boolean povMoved(Controller controller, int povIndex, PovDirection value) {
                System.out.println("#" + indexOf(controller) + ", pov " + povIndex + ": " + value);
                return false;
            }

            @Override
            public boolean xSliderMoved(Controller controller, int sliderIndex, boolean value) {
                System.out.println("#" + indexOf(controller) + ", x slider " + sliderIndex + ": " + value);
                return false;
            }

            @Override
            public boolean ySliderMoved(Controller controller, int sliderIndex, boolean value) {
                System.out.println("#" + indexOf(controller) + ", y slider " + sliderIndex + ": " + value);
                return false;
            }

            @Override
            public boolean accelerometerMoved(Controller controller, int accelerometerIndex, Vector3 value) {
                // not printing this as we get to many values
                return false;
            }
        });
    }

    private void initCellSprite(Player player, Cell cell) {
        if (cell == null) return;
        if (players.get(cell.pid) == null) return;
        if (player.fillColor == null) {
            if (graphicsHigh) cell.textureSprite = fillAtlas.createSprite(player.fill);
            if (cell.textureSprite != null) {
                cell.textureSprite.setSize(CELL_TEX_WIDTH, CELL_TEX_WIDTH);
                Vector2 pos = getHexPosition(cell.x, cell.y);
                cell.textureSprite.setCenter(pos.x, pos.y/* - (CELL_HEIGHT - CELL_WIDTH) / 2f*/);
            }
        } else {
            cell.textureSprite = null;
        }
        cell.colorSprite = mainAtlas.createSprite(TEXTURE_REGION_HEX_WHITE);
        cell.colorSprite.setColor(players.get(cell.pid).strokeColor);
        cell.colorSprite.setSize(CELL_WIDTH, CELL_HEIGHT);
        Vector2 pos = getHexPosition(cell.x, cell.y);
        cell.colorSprite.setCenter(pos.x, pos.y);
    }

    private void initPathCellSprite(Player player, Cell pathCell) {
        if (pathCell == null) return;
        if (player == null) return;
        if (players.get(pathCell.pid) == null) return;
        pathCell.colorSprite = mainAtlas.createSprite(TEXTURE_REGION_HEX_WHITE);
        pathCell.colorSprite.setColor(players.get(pathCell.pid).pathCellColor);
        pathCell.colorSprite.setSize(CELL_WIDTH, CELL_HEIGHT);
        Vector2 pos = getHexPosition(pathCell.x, pathCell.y);
        pathCell.colorSprite.setCenter(pos.x, pos.y);
    }

    /* ***************************************** DRAW *******************************************/

//    private void drawConnecting(float dt) {
//        float totalWidth = connectingText.width + LOADING_ANIMATION_SIZE + 16;
//        loadingAnimation.render(dt, batch, -totalWidth / 2f, -LOADING_ANIMATION_SIZE / 2f, LOADING_ANIMATION_SIZE, LOADING_ANIMATION_SIZE);
//        float alpha = (MathUtils.sin(4 * time) + 1) / 2f;
//        usernameFont.setColor(new Color(CONNECTING_TEXT_COLOR.r, CONNECTING_TEXT_COLOR.b, CONNECTING_TEXT_COLOR.g, alpha));
//        usernameFont.draw(batch, "Connecting...", -totalWidth / 2f + LOADING_ANIMATION_SIZE + 16, usernameFont.getLineHeight() / 2f - 4);
//    }

    private void drawConnecting(float dt) {
        float size = 20 * guiUnits;
        float gap = 8 * guiUnits;
        float x = -guiCamera.viewportWidth / 2f + 2 * guiUnits;
        float y = -guiCamera.viewportHeight / 2f + 2 * guiUnits;
        loadingAnimation.render(dt, batch, x, y, size, size);
        float alpha = (MathUtils.sin(4 * time) + 1) / 2f;
        leaderboardFont.setColor(new Color(CONNECTING_TEXT_COLOR.r, CONNECTING_TEXT_COLOR.b, CONNECTING_TEXT_COLOR.g, alpha));
        // TODO: inja yebar lango en kardam null pointer dad
        leaderboardFont.draw(batch, arFont.getText(I18N.texts[langCode][I18N.connecting]), x + size + gap, y + size / 2f + leaderboardFont.getLineHeight() / 2f - 4 * guiUnits);
    }

    private void drawTiles() {
        float firstX = leftXi * GRID_WIDTH + (bottomYi % 2 == 0 ? 0 : GRID_WIDTH / 2f);
        float firstY = bottomYi * GRID_HEIGHT;

        tiles.setX(firstX);
        tiles.setY(firstY);
        tiles.draw(batch);
    }

    private void drawTrails() {
        for (long drawPid : drawList) {
            Player player = players.get(drawPid);
            if (player == null) continue;
            if (player.status == 0 && player.trailGraphic != null) {
                player.trailGraphic.render(gameCamera.combined);
            }
        }
//        synchronized (playersMutex) {
//            for (Player player : players.valueCollection()) {
//                if (player == null) continue;
//                if (!drawList[player.pid]) continue;
//                if (player.status == 0 && player.trailGraphic != null) {
//                    player.trailGraphic.render(batch.getProjectionMatrix());
//                }
//            }
//        }
    }

    private void drawCells() {
        for (int x = leftXi; x <= leftXi + sizeX; x++) {
            if (x < -MAP_SIZE || x > MAP_SIZE) continue;
            for (int y = bottomYi; y <= bottomYi + sizeY; y++) {
                if (y < -MAP_SIZE || y > MAP_SIZE) continue;
                Cell cell = cells[x + MAP_SIZE][y + MAP_SIZE];
                Cell pathCell = pathCells[x + MAP_SIZE][y + MAP_SIZE];
                boolean hasCell = cell != null && cell.colorSprite != null;
                boolean hasPathCell = pathCell != null && pathCell.colorSprite != null;
                if (hasCell && hasPathCell) {
                    if (cell.pid == pathCell.pid) {
                        // only draw cell
                        if (players.get(cell.pid) == null) continue;
                        cell.colorSprite.draw(batch);
//                        if (DEBUG_DRAW_PIDS)
//                            usernameFont.draw(batch, cell.pid + "", cell.sprite.getX() + cell.sprite.getWidth() / 2f, cell.sprite.getY() + cell.sprite.getHeight() / 2f);
                    } else {
                        // only draw path cell
                        if (players.get(pathCell.pid) == null) continue;
                        pathCell.colorSprite.draw(batch);
//                        if (DEBUG_DRAW_PIDS)
//                            usernameFont.draw(batch, pathCell.pid + "", pathCell.sprite.getX() + pathCell.sprite.getWidth() / 2f, pathCell.sprite.getY() + pathCell.sprite.getHeight() / 2f);
                    }
                    if (!drawList.contains(cell.pid)) drawList.add(cell.pid);
                    if (!drawList.contains(pathCell.pid)) drawList.add(pathCell.pid);
                } else if (hasCell) {
                    // draw cell
                    if (players.get(cell.pid) == null) continue;
                    cell.colorSprite.draw(batch);
//                    if (DEBUG_DRAW_PIDS)
//                        usernameFont.draw(batch, cell.pid + "", cell.sprite.getX() + cell.sprite.getWidth() / 2f, cell.sprite.getY() + cell.sprite.getHeight() / 2f);
                    if (!drawList.contains(cell.pid)) drawList.add(cell.pid);
                } else if (hasPathCell) {
                    // draw path cell
                    if (players.get(pathCell.pid) == null) continue;
                    pathCell.colorSprite.draw(batch);
//                    if (DEBUG_DRAW_PIDS)
//                        usernameFont.draw(batch, pathCell.pid + "", pathCell.sprite.getX() + pathCell.sprite.getWidth() / 2f, pathCell.sprite.getY() + pathCell.sprite.getHeight() / 2f);
                    if (!drawList.contains(pathCell.pid)) drawList.add(pathCell.pid);
                }
            }
        }
    }

    private void drawTextureCells() {
        for (int x = leftXi; x <= leftXi + sizeX; x++) {
            if (x < -MAP_SIZE || x > MAP_SIZE) continue;
            for (int y = bottomYi; y <= bottomYi + sizeY; y++) {
                if (y < -MAP_SIZE || y > MAP_SIZE) continue;
                Cell cell = cells[x + MAP_SIZE][y + MAP_SIZE];
                if (cell == null || cell.colorSprite == null || cell.textureSprite == null || players.get(cell.pid) == null)
                    continue;
                Cell pathCell = pathCells[x + MAP_SIZE][y + MAP_SIZE];
                boolean hasPathCell = pathCell != null && pathCell.colorSprite != null;
                if (hasPathCell) {
                    if (cell.pid == pathCell.pid) cell.textureSprite.draw(batch);
                } else {
                    cell.textureSprite.draw(batch);
                }
            }
        }
    }

    private void drawPlayers() {
        for (long drawPid : drawList) {
            Player player = players.get(drawPid);
            if (player == null) continue;
            if (player.status == 0) {
                if (DEBUG_SHOW_GHOST && player.bcGhost != null) player.bcGhost.draw(batch);
                if (player._stroke != null) player._stroke.draw(batch);
                if (player._fill != null && !player.fillIsTexture) player._fill.draw(batch);
                if (player.indic != null) player.indic.draw(batch);
            }
        }
    }

    private void drawPlayerFillTextures() {
        for (long drawPid : drawList) {
            Player player = players.get(drawPid);
            if (player == null) continue;
            if (player.status == 0) {
                if (player._fill != null && player.fillIsTexture) player._fill.draw(batch);
            }
        }
    }

    private void drawPlayerNames() {
        for (long drawPid : drawList) {
            Player player = players.get(drawPid);
            if (player == null) continue;
            if (player.status == 0) {
                if (player._stroke != null && player.text != null) {
                    float x = player._stroke.getX() + player._stroke.getWidth() / 2f - player.text.width / 2f;
                    float y = player._stroke.getY() + 70;
                    usernameFont.setColor(player.strokeColor);
                    usernameFont.draw(batch, player._name, x, y);
                }
            }
        }
    }

    private void drawItems() {
        synchronized (room.state.items.items) {
            for (Item item : room.state.items.items.values()) {
                if (item.sprite == null) continue;

                if (item.x >= leftX && item.x <= leftX + actualWidth && item.y >= bottomY && item.y <= bottomY + actualHeight) {
                    item.sprite.draw(batch);
                }
                float x = MathUtils.lerp(item.sprite.getX() + item.sprite.getWidth() / 2f, item.x, 0.4f);
                float y = MathUtils.lerp(item.sprite.getY() + item.sprite.getHeight() / 2f, item.y, 0.4f);
                item.sprite.setCenter(x, y);
            }
        }
    }

    private void drawTextFadeOutAnimations(float dt) {
        synchronized (textFadeOutAnimations) {
            List<TextFadeOutAnimation> removeList = new ArrayList<>();
            for (TextFadeOutAnimation tfo : textFadeOutAnimations) {
                if (tfo.stop) removeList.add(tfo);
                else tfo.draw(batch, dt, usernameFont);
            }
            for (TextFadeOutAnimation tfo : removeList) {
                textFadeOutAnimations.remove(tfo);
            }
        }
    }

    private void drawProgressbar(Player player, float dt, boolean drawStatic) {
        DecimalFormat decimalFormat = new DecimalFormat("#0.0");
        float totalWidth = progressbarWidth - progressbarInitWidth;
        float percentage = player.numCells / (float) TOTAL_CELLS;
        player._percentage = MathUtils.lerp(player._percentage, percentage, 0.04f);
        float width = player._percentage * totalWidth + progressbarInitWidth;
        player.progressBar.setSize(progressbarWidth, progressbarHeight);

        player.progressBar.setX(guiCamera.viewportWidth / 2f - width);

        float y;
        if (drawStatic && player._position > (LEADERBOARD_NUM + 1)) {
            y = guiCamera.viewportHeight / 2f - progressbarTopMargin - LEADERBOARD_NUM * (progressbarHeight + progressbarGap) - progressbarHeight - progressbarExtraGapForCurrentPlayer;
        } else {
            y = guiCamera.viewportHeight / 2f - progressbarTopMargin - (player._position - 1) * (progressbarHeight + progressbarGap) - progressbarHeight;
        }


        if (drawStatic || !player.positionIsChanging) {
            player.progressBar.setY(y);
        } else {
            if (player.progressBar.getY() < y) {
                player.progressBar.translateY(dt * leaderboardChangeSpeed);
                if (player.progressBar.getY() > y) {
                    player.progressBar.setY(y);
                    player.positionIsChanging = false;
                }
            } else {
                player.progressBar.translateY(-dt * leaderboardChangeSpeed);
                if (player.progressBar.getY() < y) {
                    player.progressBar.setY(y);
                    player.positionIsChanging = false;
                }
            }
        }
        player.progressBar.draw(batch);

        if (player.positionIsChanging && player.changeDir == Player.CHANGE_DIRECTION_DOWN) return;

        FontDrawItem fdi = new FontDrawItem();
        fdi.color = player.strokeColor;
        fdi.text = player._position + "- " + decimalFormat.format(percentage * 100f) + "% " + player._name;
        fdi.x = player.progressBar.getX() + 6 * guiUnits;
        fdi.y = player.progressBar.getY() + (progressbarHeight + leaderboardFont.getLineHeight()) / 2f - 2 * guiUnits;
        leaderboardDrawList.add(fdi);
    }

    private void drawLeaderboard(float dt) {

        synchronized (leaderboardList) {

            // sort players by numCells
            Collections.sort(leaderboardList, SORT_PLAYERS_BY_NUM_CELLS);

            short kk = 1;
            for (Player cm : leaderboardList) {
                cm.position = kk++;
            }

            Collections.sort(leaderboardList, SORT_PLAYERS_BY_POSITION);

            if (!leaderboardDrawAgain) {
                List<Integer> _positions = new ArrayList<>();
                boolean allWrong = false;
                for (Player player : leaderboardList) {
                    if (_positions.contains(player._position)) {
                        allWrong = true;
                        break;
                    }
                    _positions.add(player._position);
                }
                if (allWrong) {
                    int x = 1;
                    for (Player player : leaderboardList) {
                        player._position = x++;
                    }
                    leaderboardDrawAgain = true;
                }
            }

            if (leaderboardDrawAgain) {
                for (Player player : leaderboardList) {
                    player.positionIsChanging = false;
                    if (player.progressBar != null) {
                        player.progressBar.setX(Gdx.graphics.getWidth() / 2f - (player._percentage * (progressbarWidth - progressbarInitWidth) + progressbarInitWidth));
                        player.progressBar.setY(Gdx.graphics.getHeight() / 2f - progressbarTopMargin - Math.min(player._position - 1, LEADERBOARD_NUM) * (progressbarHeight + progressbarGap) - progressbarHeight);
                    }
                }
                leaderboardDrawAgain = false;
            } else {

                for (int i = 0; i < leaderboardList.size() - 1; i++) {
                    Player player = leaderboardList.get(i);
                    if (player.positionIsChanging) continue;
                    if (player.position > player._position) { // yani bayad bere paeen
                        Player next = leaderboardList.get(i + 1);
                        if (!next.positionIsChanging && next.position <= next._position) {
                            if (player._position <= LEADERBOARD_NUM) {
                                if (next._position <= LEADERBOARD_NUM + 1) {
                                    player.positionIsChanging = true;
                                    player.changeDir = Player.CHANGE_DIRECTION_DOWN;
                                    next.positionIsChanging = true;
                                    next.changeDir = Player.CHANGE_DIRECTION_UP;
                                }
                            }
                            player._position++;
                            next._position--;
                            i++;
                        }
                    }
                }

                Collections.sort(leaderboardList, SORT_PLAYERS_BY_POSITION);
            }

            boolean playerProgressPrinted = false;
//            Player currentPlayer = room.state.players.get(room.getSessionId());
            int limit = Math.min(LEADERBOARD_NUM + 1, leaderboardList.size());
            for (int i = limit - 1; i >= 0; i--) {
                Player player = leaderboardList.get(i); // _position = i + 1
                if (player == null || player.progressBar == null) continue;
                if (i == LEADERBOARD_NUM) {
                    if (!player.positionIsChanging) continue;
                    if (player.changeDir == Player.CHANGE_DIRECTION_UP) continue;
                }
                if (players.get(player.pid) == null) {
                    player.positionIsChanging = false;
                    continue;
                }
                drawProgressbar(player, dt, false);
                if (currentPlayer != null && currentPlayer.pid == player.pid)
                    playerProgressPrinted = true;
            }

            if (!playerProgressPrinted) {
                if (currentPlayer == null || currentPlayer.progressBar == null) return;
                drawProgressbar(currentPlayer, dt, true);
            }
        }

    }

    private void drawPlayerProgress() {
//        Player currentPlayer = room.state.players.get(room.getSessionId());
        if (currentPlayer == null || currentPlayer.strokeColor == null) return;

        DecimalFormat decimalFormat = new DecimalFormat("#0.0");
        float totalWidth = yourProgressbarWidth - yourProgressbarInitWidth;

        playerProgressBarBest.setSize(playerBestProgress * totalWidth + yourProgressbarInitWidth, progressbarHeight);
        playerProgressBarBest.setX(-guiCamera.viewportWidth / 2f);
        playerProgressBarBest.setY(guiCamera.viewportHeight / 2f - progressbarTopMargin - progressbarHeight);

        playerProgressBar.setSize(currentPlayer._percentage * totalWidth + yourProgressbarInitWidth, progressbarHeight);
        playerProgressBar.setX(-guiCamera.viewportWidth / 2f);
        playerProgressBar.setY(guiCamera.viewportHeight / 2f - progressbarTopMargin - progressbarHeight);

        playerProgressBarBest.draw(batch);
        playerProgressBar.draw(batch);

        FontDrawItem fdi = new FontDrawItem();
        fdi.color = currentPlayer.strokeColor;
        fdi.text = (currentPlayer._percentage < 0.1f ? " " : "") + decimalFormat.format(currentPlayer._percentage * 100f) + "%";
        fdi.x = playerProgressBar.getX() + playerProgressBar.getWidth() - yourProgressText.width + guiUnits * 8;
        fdi.y = playerProgressBar.getY() + (progressbarHeight + leaderboardFont.getLineHeight()) / 2f - 2 * guiUnits;
        leaderboardDrawList.add(fdi);

        fdi = new FontDrawItem();
        fdi.color = COLOR_YOUR_BEST_PROGRESS_TEXT;
        fdi.text = "BEST " + decimalFormat.format(playerBestProgress * 100) + "%";
        fdi.x = playerProgressBarBest.getX() + playerProgressBarBest.getWidth() - yourProgressBestText.width + guiUnits * 8;
        fdi.y = playerProgressBarBest.getY() - 2 * guiUnits;
        leaderboardDrawList.add(fdi);
    }

    private void drawCoin() {
        coin.setSize(progressbarHeight * 0.8f, progressbarHeight * 0.8f);
        coin.setX(-Gdx.graphics.getWidth() / 2f + 8 * guiUnits);
        coin.setY(guiCamera.viewportHeight / 2f - 70 * guiUnits);
        coin.draw(batch);
    }

    private void drawCoinText() {
        leaderboardFont.setColor(COLOR_YOUR_BEST_PROGRESS_TEXT);
        leaderboardFont.draw(batch, coinValue + "", -Gdx.graphics.getWidth() / 2f + coin.getWidth() + 15 * guiUnits, guiCamera.viewportHeight / 2f - 73 * guiUnits + leaderboardFont.getLineHeight());
    }

    private void drawTime() {
        long remainingTime = room.state.endTime - getServerTime();
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

//    private void drawKills() {
//        if (currentPlayer == null) return;
//        ColorMeta colorMeta = room.state.colorMeta.get(currentPlayer.color + "");
//        if (colorMeta == null) return;
//        float x = -Gdx.graphics.getWidth() / 2f;
//        float y = Gdx.graphics.getHeight() / 2f - 120 * guiUnits;
//        killsBg.setSize(120 * guiUnits, 50 * guiUnits);
//        killsBg.setPosition(x, y);
//        killsBg.draw(batch);
//        statsFont.setColor(Color.WHITE);
//        statsFont.draw(batch, "Blocks : " + colorMeta.numCells, x + 4 * guiUnits, y + killsBg.getHeight() - 8 * guiUnits);
//        statsFont.draw(batch, "Kills : " + currentPlayer.kills, x + 4 * guiUnits, y + killsBg.getHeight() - 1 * statsFont.getLineHeight() - 8 * guiUnits);
//    }

    private void drawYouWillRespawnText() {
//        Player player = room.state.players.get(room.getSessionId());
        if (currentPlayer == null || currentPlayer.status != 1) return;
        int remainingTime = (int) (currentPlayer.rspwnTime - getServerTime());
        int seconds = remainingTime / 1000;
        if (seconds < 0 || seconds > 9) return;
        float x = -youWillRspwnText.width / 2f;
        float y = Gdx.graphics.getHeight() / 4f;
        youWillRspwnBg.setSize(youWillRspwnText.width + 12 * guiUnits, youWillRspwnText.height + 12 * guiUnits);
        youWillRspwnBg.setPosition(x - 6 * guiUnits, y - youWillRspwnText.height - 6 * guiUnits);
        youWillRspwnBg.draw(batch);
        timeFont.draw(batch, "You will respawn in " + seconds + " seconds", x, y);
    }

    private void drawPing() {
        String logText = "fps: " + Gdx.graphics.getFramesPerSecond();
        if (currentPing > 0) {
            logText += " - ping: " + currentPing;
        }
        logFont.draw(batch, logText, -Gdx.graphics.getWidth() / 2f + 8 * guiUnits, -Gdx.graphics.getHeight() / 2f + 2 * guiUnits + logFont.getLineHeight());
    }

    private void drawStatsBackground() {
        statsBg.setSize(70 * guiUnits, progressbarHeight);
        statsBg.setPosition(-guiCamera.viewportWidth / 2f, guiCamera.viewportHeight / 2f - 100 * guiUnits);
        statsBg.draw(batch);


        coin.setSize(10 * guiUnits, 10 * guiUnits);
        coin.setX(statsBg.getX() + 5 * guiUnits);
        coin.setCenterY(statsBg.getY() + statsBg.getHeight() / 2f);
        coin.draw(batch);

        statsBg.setSize(100 * guiUnits, progressbarHeight);
        statsBg.setPosition(-guiCamera.viewportWidth / 2f, statsBg.getY() - statsBg.getHeight() - 10 * guiUnits);
        statsBg.draw(batch);
        statsBg.setSize(100 * guiUnits, progressbarHeight);
        statsBg.setPosition(-guiCamera.viewportWidth / 2f, statsBg.getY() - statsBg.getHeight());
        statsBg.draw(batch);


    }

    private void drawStatsText() {
        if (currentPlayer == null) return;
        float y = guiCamera.viewportHeight / 2f - 100 * guiUnits + progressbarHeight / 2f + 3 * guiUnits;
        statsFont.setColor(Color.WHITE);
//        statsFont.draw(batch, "Coins: " + coinValue, statsBg.getX() + 5 * guiUnits, y);
        statsFont.draw(batch, "" + coinValue, statsBg.getX() + 20 * guiUnits, y);
        statsFont.draw(batch, "Blocks: " + currentPlayer.numCells, statsBg.getX() + 5 * guiUnits, y - progressbarHeight - 10 * guiUnits);
        statsFont.draw(batch, "Kills: " + currentPlayer.kills, statsBg.getX() + 5 * guiUnits, y - 2 * progressbarHeight - 10 * guiUnits);

    }

    /* ***************************************** LOGIC *******************************************/

    private void updatePlayersPositions(float dt) {
//        synchronized (players) {
//        for (int i = 0; i < players.length; i++) {
//            Player player = players[i];
        synchronized (playersMutex) {
            for (Player player : players.valueCollection()) {
                if (player == null || player._stroke == null) continue;

                if (player.status == 0) {

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

                    player._angle += _Math.angleDistance(player._angle, player.new_angle) * PLAYER_ROTATE_SPEED * dt;

                    float x = player._stroke.getX() + player._stroke.getWidth() / 2f;
                    float y = player._stroke.getY() + player._stroke.getHeight() / 2f;

                    float newX = x + MathUtils.cos(player.angle) * player.speed * dt;
                    float newY = y + MathUtils.sin(player.angle) * player.speed * dt;

                    if (newX <= MAP_SIZE_X_EXT_PIXEL && newX >= -MAP_SIZE_X_EXT_PIXEL) x = newX;
                    if (newY <= MAP_SIZE_Y_EXT_PIXEL && newY >= -MAP_SIZE_Y_EXT_PIXEL) y = newY;

                    player._stroke.setCenter(x, y);

                    player._fill.setCenter(x, y);

                    if (player.indic != null) {
                        player.indic.setCenter(x, y);
                        player.indic.setRotation(player._angle * MathUtils.radiansToDegrees - 90);
                    }

                    player.bcGhost.setCenter(player.x, player.y);

                    if (ROTATE_CAMERA) {
                        player._fill.setOriginCenter();
                        player._fill.setRotation(player._angle * MathUtils.radiansToDegrees - 90);
                    }

//                    if (ADD_FAKE_PATH_CELLS) {
//                        if (room.state.started && !room.state.ended)
//                            processPlayerPosition(player, x, y);
//                    }
                } else {
                    float x = player.x;
                    float y = player.y;

                    player._stroke.setCenter(x, y);
                    player._fill.setCenter(x, y);
                    if (player.indic != null) {
                        player.indic.setCenter(x, y);
                        player.indic.setRotation(player.angle * MathUtils.radiansToDegrees - 90);
                    }
                    player.bcGhost.setCenter(player.x, player.y);
                }

            }
        }
//        }
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
        if (currentPlayer == null) return;
        gameCamera.zoom = Math.min(CAMERA_INIT_ZOOM + currentPlayer.numCells * 0.0008f, 1.5f);
    }

    private long getServerTime() {
        return System.currentTimeMillis() + timeDiff;
    }

    private void correctPlayerPosition(Player player) {

        float lerp = calculateLerp(player);

//                    float d = dst > 75 ? dst : dst > 25 ? 5f : Math.min(dst, 1.25f);

        if (lerp > 0) {
            if (player.clientId.equals(room.getSessionId())) {
                System.out.println("lerp is " + lerp);
            }
            player._stroke.setCenterX(MathUtils.lerp(player._stroke.getX() + player._stroke.getWidth() / 2f, player.x, lerp));
            player._stroke.setCenterY(MathUtils.lerp(player._stroke.getY() + player._stroke.getHeight() / 2f, player.y, lerp));
            player._fill.setCenter(player._stroke.getX() + player._stroke.getWidth() / 2f, player._stroke.getY() + player._stroke.getHeight() / 2f);
            if (player.indic != null) {
                player.indic.setCenter(player._stroke.getX() + player._stroke.getWidth() / 2f, player._stroke.getY() + player._stroke.getHeight() / 2f);
            }
            if (lerp > 0.5f) {
                player._angle = player.angle;
            }
        }
    }

    private float calculateLerp(Player player) {
        // TODO: add alternative lerp/move methods
//        if (connectTime > HIGH_LERP_TIME) {
//            // normal
//            float dst = Vector2.dst2(player.bc.getX() + player.bc.getWidth() / 2f, player.bc.getY() + player.bc.getHeight() / 2f, player.x, player.y);
//            if (dst > 5625) {
//                return 1;
//            } else if (dst > 625) {
//                return 100f / dst;
//            } else if (dst > 60) {
//                return 10 / dst;
//            } else {
//                if (player.clientId.equals(room.getSessionId())) System.out.println("NO LERPING");
//                return 0;
//            }
//        } else {
        // high lerp time

        float dst = Vector2.dst2(player._stroke.getX() + player._stroke.getWidth() / 2f, player._stroke.getY() + player._stroke.getHeight() / 2f, player.x, player.y);

        // float d = dst > 75 ? dst : dst > 25 ? 5f : Math.min(dst, 1.25f);

        if (dst > 5625) { // > 75
            return 1;
        } else if (dst > 625) { // > 25
            return 100f / dst;
        } else if (dst > 25) { // > 5
            return 10f / dst;
        } else {
            //if (player.clientId.equals(room.getSessionId())) System.out.println("NO LERPING");
//                return 0.3f;
            return 0;
        }

//        float l = 1 + (dst - 5625) / 6222f;
//        if (l > 1) return 1;
//        if (l < 0.1f) {
//            if (player.clientId.equals(room.getSessionId())) System.out.println("NO LERPING");
//            return 0;
//        }
//        return l;
    }

    private Vector2 getHexPosition(int x, int y) {
        Vector2 pos = new Vector2();
        pos.x = x * GRID_WIDTH + (y % 2 == 0 ? 0 : GRID_WIDTH / 2f);
        pos.y = y * GRID_HEIGHT;
        return pos;
    }

    private void handleTouchDownDrag(int screenX, int screenY) {
        if (controllerType == Constants.CONTROL_TOUCH) {
            direction = (int) Math.toDegrees(Math.atan2(screenY, (float) screenX));
        } else if (controllerType == Constants.CONTROL_FLOATING && mouseIsDown) {
            thumbstickBgSprite.setCenter(padAnchorPoint.x, padAnchorPoint.y);
            padVector.set(screenX - padAnchorPoint.x, screenY - padAnchorPoint.y);
            if (padVector.len2() > PAD_CONTROLLER_MAX_LENGTH * PAD_CONTROLLER_MAX_LENGTH) {
                padVector.nor().scl(PAD_CONTROLLER_MAX_LENGTH);
            }
            thumbstickPadSprite.setCenter(padAnchorPoint.x + padVector.x, padAnchorPoint.y + padVector.y);
            direction = (int) Math.toDegrees(Math.atan2(padVector.y, padVector.x));
        } else if (controllerType == Constants.CONTROL_FIXED_LEFT || controllerType == Constants.CONTROL_FIXED_RIGHT) {
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
        yourProgressbarWidth = lessValue * 0.4f;
        progressbarInitWidth = lessValue * 0.25f;
        yourProgressbarInitWidth = lessValue * 0.15f;
        guiUnits = lessValue * 0.002f;

        progressbarGap = -1;//lessValue / 400f;
        progressbarHeight = lessValue / 18f;
        progressbarTopMargin = 0;//lessValue / 125f;
        progressbarExtraGapForCurrentPlayer = progressbarHeight + lessValue / 100f;
    }

    /* **************************************** NETWORK ******************************************/

    private String getRoomName() {
        if (gameMode == GAME_MODE_BATTLE) {
            return "battle";
        } else if (gameMode == GAME_MODE_FFA) {
            return "ffa";
        }
        return "";
    }

    private void connectToServer() {
        System.out.println("ConnectToServer...");
        connectionState = CONNECTION_STATE_CONNECTING;

        Client client = new Client(Constants.GAME_ENDPOINT);
        LinkedHashMap<String, Object> options = new LinkedHashMap<>();
        options.put("name", prefs.getString(Constants.KEY_PLAYER_NAME, ""));
        //String fill = "e" + TextUtil.padLeftZeros((int) (Math.random() * 100) + "", 5);
        int selectedFill = prefs.getInteger(Constants.KEY_SELECTED_IMAGE_INDEX, 0);
        System.out.println("TTTTTTTTTTTTTT PLAYSCREEN: selected image = " + selectedFill);
        if (selectedFill >= 0) {
            String fill = "e" + TextUtil.padLeftZeros("" + selectedFill, 5);
            options.put("fill", fill);
            System.out.println("TTTTTTTTTTTTTT fill is " + fill);
        } else {
            int sc = ColorUtil.FILL_COLORS[prefs.getInteger(Constants.KEY_SELECTED_COLOR, 0)] & 0xFFFFFF;
            sc = sc << 8;
            sc = sc | 0xFF;
            options.put("fill", "#" + new Color(sc));
        }

//        fill = "e00093";
        // System.out.println("fill >>> " + fill);
        int sc = ColorUtil.STROKE_COLORS[prefs.getInteger(Constants.KEY_SELECTED_COLOR, 0)] & 0xFFFFFF;
        sc = sc << 8;
        sc = sc | 0xFF;
        options.put("stroke", "#" + new Color(sc));
        String id = prefs.getString(Constants.KEY_ID, null); // TODO: id on pref? really nigga?
        if (id != null) options.put("id", id);
        if (sessionId == null) {
            client.joinOrCreate(getRoomName(), MyState.class, options, this::updateRoom, e -> {
                if (e.getMessage().equals("Not Found")) {
                    // id not found
                    System.out.println(" - id not found - ");
                    prefs.remove(Constants.KEY_ID);
                    prefs.flush();
                }
                e.printStackTrace();
                if (connectionState != CONNECTION_STATE_CLOSED)
                    connectionState = CONNECTION_STATE_DISCONNECTED;
            });
        } else {
            client.reconnect(roomId, sessionId, MyState.class, this::updateRoom, e -> {
                if (e instanceof MatchMakeException) {
                    this.roomId = null;
                    this.sessionId = null;
                } else if (e.getMessage().equals("Not Found")) {
                    // id not found
                    System.out.println(" - id not found - ");
                    prefs.remove(Constants.KEY_ID);
                    prefs.flush();
                }
                e.printStackTrace();
                if (connectionState != CONNECTION_STATE_CLOSED)
                    connectionState = CONNECTION_STATE_DISCONNECTED;
            });
        }
    }

    private void updateRoom(Room<MyState> room) {
        this.room = room;
        this.sessionId = room.getSessionId();
        this.roomId = room.getId();

        System.out.println("joined " + getRoomName());

        players.clear();
        for (int i = 0; i < MAP_SIZE * 2 + 1; i++) {
            for (int j = 0; j < MAP_SIZE * 2 + 1; j++) {
                cells[i][j] = null;
                pathCells[i][j] = null;
            }
        }

        connectionState = CONNECTION_STATE_CONNECTED;
        isUpdating = true;
        registerCallbacks();

        room.setListener(new Room.Listener() {

            @Override
            protected void onLeave(int code) {
                System.out.println("left " + getRoomName() + " code = " + code);
                if (code > 1000) {
                    if (connectionState != CONNECTION_STATE_CLOSED)
                        connectionState = CONNECTION_STATE_DISCONNECTED;
                }
            }

            @Override
            protected void onError(Exception e) {
                if (connectionState != CONNECTION_STATE_CLOSED)
                    connectionState = CONNECTION_STATE_DISCONNECTED;
                System.out.println("onError()");
                e.printStackTrace();
            }

            @Override
            protected void onMessage(Object message) {
                if (connectionState != CONNECTION_STATE_CONNECTED) return;
                System.out.println("onMessage()");
                System.out.println(message);
                LinkedHashMap<String, Object> data = (LinkedHashMap<String, Object>) message;
                if (data.get("op").equals("wlc")) {
                    long time = (long) data.get("time");
                    timeDiff = time - System.currentTimeMillis();
                    System.out.println("time diff: " + timeDiff);
                    String id = (String) data.get("id");
                    System.out.println("id = " + id);
                    prefs.putString(Constants.KEY_ID, id).flush();
                } else if (data.get("op").equals("cp")) {
                    if (connectionState == CONNECTION_STATE_CLOSED) return;
                    String clientId = (String) data.get("player");
                    int num = (int) data.get("num");
                    Player player = room.state.players.get(clientId);
                    if (player != null) {
                        if (player.trailGraphic != null) {
                            Gdx.app.postRunnable(() -> player.trailGraphic.truncateAt(0));
                        }
                        Timer.schedule(new Timer.Task() {
                            @Override
                            public void run() {
//                                        for (int x = leftXi; x <= leftXi + sizeX; x++) {
//                                            if (x < -MAP_SIZE || x > MAP_SIZE) continue;
//                                            for (int y = bottomYi; y <= bottomYi + sizeY; y++) {
//                                                if (y < -MAP_SIZE || y > MAP_SIZE) continue;
//                                                Cell cell = pathCells[x + MAP_SIZE][y + MAP_SIZE];
//                                                if (cell != null && cell.pid == player.pid) {
//                                                    pathCells[x + MAP_SIZE][y + MAP_SIZE] = null;
//                                                }
//                                            }
//                                        }

                                for (int i = 0; i < 2 * MAP_SIZE + 1; i++) {
                                    for (int j = 0; j < 2 * MAP_SIZE + 1; j++) {
                                        Cell pathCell = pathCells[i][j];
                                        if (pathCell != null && pathCell.pid == player.pid) {
                                            pathCells[i][j] = null;
                                        }
                                    }
                                }
                            }
                        }, 0.3f);
                    }
                    if (clientId.equals(room.getSessionId())) {
                        if (num > 4) {
                            if (soundIsOn) Gdx.app.postRunnable(() -> captureSound.play());
                            synchronized (textFadeOutAnimations) {
                                Sprite playerSprite = room.state.players.get(clientId)._stroke;
                                textFadeOutAnimations.add(new TextFadeOutAnimation("+" + num + " blocks", COLOR_BLOCKS_TEXT_FADE_OUT, playerSprite.getX() + playerSprite.getWidth() / 2f, playerSprite.getY() + playerSprite.getHeight() / 2f));
                            }
                        }
                        Gdx.app.log(TAG, "+" + num + " blocks");
                    }
                } else if (data.get("op").equals("pg")) {
                    long t = (long) data.get("t");
                    long st = (long) data.get("st");
                    long currentTimeMillis = System.currentTimeMillis();
                    timeDiff = st - currentTimeMillis;
                    // System.out.println("new timeDiff = " + timeDiff);
                    if (t == lastPingSentTime) {
                        lastPingReplyTime = currentTimeMillis;
                        currentPing = (int) (lastPingReplyTime - t);
                    } else currentPing = 0;
                } else if (data.get("op").equals("dt")) {
                    // dead
                    connectionState = CONNECTION_STATE_CLOSED;
                    double x = Double.valueOf(data.get("x") + "");
                    double y = Double.valueOf(data.get("y") + "");
                    deathPosition.set((float) x, (float) y);
                    Gdx.app.postRunnable(() -> {
                        if (soundIsOn) deathSound.play();
                        gameCamera.zoom = 1;
                    });
                    System.out.println("YOU ARE DEAD!");
                    // TODO: show death dialog
                } else if (data.get("op").equals("ht")) {
                    // dead
                    if (soundIsOn) Gdx.app.postRunnable(() -> hitSound.play());
                } else if (data.get("op").equals("et")) {
                    int itemType = (int) data.get("type");
                    if (itemType == Item.TYPE_COIN) {
                        if (soundIsOn) Gdx.app.postRunnable(() -> coinSound.play());
                        coinValue = (int) data.get("coins");
                        prefs.putInteger(Constants.KEY_COINS, coinValue).flush();
                        int add = (int) data.get("add");
                        synchronized (textFadeOutAnimations) {
                            Sprite playerSprite = room.state.players.get(room.getSessionId())._stroke;
                            textFadeOutAnimations.add(new TextFadeOutAnimation("+" + add + " coins", COLOR_COINS_TEXT_FADE_OUT, playerSprite.getX() + playerSprite.getWidth() / 2f, playerSprite.getY() + playerSprite.getHeight() / 2f));
                        }
                    } else if (itemType == Item.TYPE_BOOST) {
                        if (soundIsOn) Gdx.app.postRunnable(() -> boostSound.play());
                    }
                }
            }

            @Override
            protected void onStateChange(Schema state, boolean isFirstState) {
                if (isFirstState) {
                    Timer.schedule(new Timer.Task() {
                        @Override
                        public void run() {
                            message.put("op", "r");
                            room.send(message);
                            isUpdating = false;
                        }
                    }, 1f);
                }
            }
        });
    }

    private void registerCallbacks() {
        room.state.players.onAdd = (player, key) -> {
            if (connectionState != CONNECTION_STATE_CONNECTED) return;
            if (player.pid == 0) return;

            if (player.clientId.equals(room.getSessionId())) {
                System.out.println("OOOOOOOOOOO YOU HAVE BEEN ADDED YOO HAHAHAHA");
            }

            synchronized (arFont) {
                player._name = arFont.getText(player.name);
            }
            player._angle = player.angle;

            Gdx.app.postRunnable(() -> {

                if (player.stroke.startsWith("#") && player.stroke.length() == 9) {
                    player.strokeColor = Color.valueOf(player.stroke);
                    player.progressColor = ColorUtil.getPlayerProgressColor(player.strokeColor);
                    player.pathCellColor = ColorUtil.getPlayerPathCellColor(player.strokeColor);
                } else {
                    player.strokeColor = Color.BLACK;
                    player.progressColor = Color.BLACK;
                    player.pathCellColor = Color.BLACK;
                }

                if (player.clientId.equals(room.getSessionId())) {
                    System.out.println("TTTTTTTTTTTTTT player fill is " + player.fill);
                }

                if (player.fill.startsWith("#") && player.fill.length() == 9) {
                    player.fillColor = Color.valueOf(player.fill);
                    player.progressColor = player.fillColor;
                } else if (player.progressColor.equals(Color.BLACK)) {
                    System.out.println(player.stroke + " " + player.fill);
                }

                player.text = new GlyphLayout(usernameFont, player._name);

                player._stroke = mainAtlas.createSprite(TEXTURE_REGION_BC);
                player._stroke.setSize(STROKE_SIZE, STROKE_SIZE);
                player._stroke.setColor(player.strokeColor);
                player._stroke.setCenter(player.x, player.y);

                if (player.fillColor != null) {
                    player._fill = mainAtlas.createSprite(TEXTURE_REGION_BC);
                    player._fill.setColor(player.fillColor);
                    player.fillIsTexture = false;
                } else {
                    if (graphicsHigh) {
                        player._fill = fillAtlas.createSprite(player.fill);
                        if (player.clientId.equals(room.getSessionId())) {
                            System.out.println("TTTTTTTTTTTTTT player fill is " + player.fill);
                        }
                    }
                    if (player._fill == null) {
                        player._fill = mainAtlas.createSprite(TEXTURE_REGION_BC);
                        player._fill.setColor(player.progressColor);
                        player.fillIsTexture = false;
                    } else {
                        player.fillIsTexture = true;
                    }
                }

                player._fill.setSize(FILL_SIZE, FILL_SIZE);
                player._fill.setCenter(player.x, player.y);

                if (player.clientId.equals(room.getSessionId())) {
                    playerProgressBar.setColor(player.progressColor);
                    player.indic = mainAtlas.createSprite(TEXTURE_REGION_INDIC);
                    player.indic.setSize(INDIC_SIZE, INDIC_SIZE);
                    player.indic.setColor(player.strokeColor);
                    player.indic.setCenter(player.x, player.y);
                    player.indic.setOriginCenter();
                    player.indic.setRotation(player.angle * MathUtils.radiansToDegrees - 90);
                }

                player.bcGhost = mainAtlas.createSprite(TEXTURE_REGION_BC);
                player.bcGhost.setColor(player.strokeColor.r, player.strokeColor.g, player.strokeColor.b, player.strokeColor.a / 2f);
                player.bcGhost.setCenter(player.x, player.y);
                player.bcGhost.setSize(STROKE_SIZE, STROKE_SIZE);

                if (player.clientId.equals(room.getSessionId())) {
                    gameCamera.position.x = player.x;
                    gameCamera.position.y = player.y;
                }

                player.trailGraphic = new TrailGraphic(trailTexture);
                player.trailGraphic.setTint(player.strokeColor);
                player.trailGraphic.setRopeWidth(ROPE_WIDTH);
                player.trailGraphic.setTextureULengthBetweenPoints(1 / 2f);

                player._position = leaderboardList.size() + 1;
                player._percentage = player.numCells / (float) TOTAL_CELLS;
                player.progressBar = mainAtlas.createSprite(TEXTURE_REGION_PROGRESSBAR);
                player.progressBar.setColor(player.progressColor);
                player.progressBar.setX(Gdx.graphics.getWidth() / 2f - (player._percentage * (progressbarWidth - progressbarInitWidth) + progressbarInitWidth));
                player.progressBar.setY(Gdx.graphics.getHeight() / 2f - progressbarTopMargin - Math.min(player._position - 1, LEADERBOARD_NUM) * (progressbarHeight + progressbarGap) - progressbarHeight);

                players.put(player.pid, player);
                synchronized (leaderboardList) {
                    if (!leaderboardList.contains(player)) leaderboardList.add(player);
                }
                registerPlayerCallbacks(player);
            });
        };
        room.state.players.onRemove = (player, key) -> {
            if (connectionState != CONNECTION_STATE_CONNECTED) return;
            if (player.pid == 0) return;
            System.out.println("player removed, color: " + player.pid);
            if (player.trailGraphic != null) {
                Gdx.app.postRunnable(() -> player.trailGraphic.truncateAt(0));
            }
//                            for (int x = leftXi; x <= leftXi + sizeX; x++) {
//                                if (x < -MAP_SIZE || x > MAP_SIZE) continue;
//                                for (int y = bottomYi; y <= bottomYi + sizeY; y++) {
//                                    if (y < -MAP_SIZE || y > MAP_SIZE) continue;
//                                    Cell cell = pathCells[x + MAP_SIZE][y + MAP_SIZE];
//                                    if (cell != null && cell.pid == player.pid) {
//                                        pathCells[x + MAP_SIZE][y + MAP_SIZE] = null;
//                                    }
//                                    cell = cells[x + MAP_SIZE][y + MAP_SIZE];
//                                    if (cell != null && cell.pid == player.pid) {
//                                        cells[x + MAP_SIZE][y + MAP_SIZE] = null;
//                                    }
//                                }
//                            }
            for (int i = 0; i < 2 * MAP_SIZE + 1; i++) {
                for (int j = 0; j < 2 * MAP_SIZE + 1; j++) {
                    Cell cell = pathCells[i][j];
                    if (cell != null && cell.pid == player.pid) {
                        pathCells[i][j] = null;
                    }
                    cell = cells[i][j];
                    if (cell != null && cell.pid == player.pid) {
                        cells[i][j] = null;
                    }
                }
            }
            players.remove(player.pid);
            synchronized (leaderboardList) {
                leaderboardList.remove(player);
            }
        };

        room.state.items.onAdd = (item, key) -> Gdx.app.postRunnable(() -> {
            if (connectionState != CONNECTION_STATE_CONNECTED) return;
            if (item.type == Item.TYPE_COIN) {
                item.sprite = mainAtlas.createSprite(TEXTURE_REGION_COIN);
            } else if (item.type == Item.TYPE_BOOST) {
                item.sprite = mainAtlas.createSprite(TEXTURE_REGION_BOOST);
            }
            item.sprite.setCenter(item.x, item.y);
        });

        room.state.players.triggerAll();
        room.state.items.triggerAll();
    }

    void registerPlayerCallbacks(Player player) {
        player.path.onAdd = (point, key) -> {
            if (connectionState != CONNECTION_STATE_CONNECTED) return;
            if (player.trailGraphic == null) return;
            Gdx.app.postRunnable(() -> {
                Point lastPoint = player.path.get(key - 1);
                if (lastPoint != null) {
                    float dx = point.x - lastPoint.x;
                    float dy = point.y - lastPoint.y;
//                                        player.trailGraphic.setPoint(key2 * 4 - 1, lastPoint.x + 3 * dx / 4f, lastPoint.y + 3 * dy / 4f);
//                                        player.trailGraphic.setPoint(key2 * 4 - 2, lastPoint.x + 2 * dx / 4f, lastPoint.y + 2 * dy / 4f);
//                                        player.trailGraphic.setPoint(key2 * 4 - 3, lastPoint.x + 1 * dx / 4f, lastPoint.y + 1 * dy / 4f);
                    player.trailGraphic.setPoint((key - 1) * 2, lastPoint.x, lastPoint.y);
                    player.trailGraphic.setPoint(key * 2 - 1, lastPoint.x + dx / 2f, lastPoint.y + dy / 2f);
                    player.trailGraphic.setPoint(key * 2, point.x + dx / 2f, point.y + dy / 2f);
                }
            });
        };

        player.path_cells.onAdd = (cell, key) -> {
            if (connectionState != CONNECTION_STATE_CONNECTED) return;
//                            System.out.println("new path cell for player " + player.pid + " name= " + player.name + " cell.pid= " + cell.pid);
            Gdx.app.postRunnable(() -> initPathCellSprite(player, cell));
            pathCells[cell.x + MAP_SIZE][cell.y + MAP_SIZE] = cell;
        };

        player.cells.onAdd = (cell, key) -> {
            if (connectionState != CONNECTION_STATE_CONNECTED) return;
            Gdx.app.postRunnable(() -> initCellSprite(player, cell));
            cells[cell.x + MAP_SIZE][cell.y + MAP_SIZE] = cell;
        };

        player.cells.onRemove = (cell, key) -> {
            if (connectionState != CONNECTION_STATE_CONNECTED) return;
//                            System.out.println("cell remove key : " + key + " --> " + cell.x + ", " + cell.y);
            Cell c = cells[cell.x + MAP_SIZE][cell.y + MAP_SIZE];
            if (c == null || c.pid != player.pid) return;
            cells[cell.x + MAP_SIZE][cell.y + MAP_SIZE] = null;
        };

        player.cells.triggerAll();
        player.path_cells.triggerAll();
        player.path.triggerAll();
    }

    private void checkConnection() {
        if (connectionState == CONNECTION_STATE_CONNECTED && lastPingReplyTime > 0 && System.currentTimeMillis() - lastPingReplyTime > 15000) {
            // we are disconnected for sure
            if (connectionState != CONNECTION_STATE_CLOSED)
                connectionState = CONNECTION_STATE_DISCONNECTED;
        }
        if (connectionState == CONNECTION_STATE_DISCONNECTED) {
            connectToServer();
        }
    }

    private void sendDirection() {
        if (room == null || !room.hasJoined()) return;
        if (direction != -1000 && lastDirection != direction) {
            message.put("op", "d");
            message.put("v", direction);
            room.send(message);
            lastDirection = direction;
        }
    }

    private void sendPing() {
        if (room == null || !room.hasJoined()) return;
        lastPingSentTime = System.currentTimeMillis();
        message.put("op", "p");
        message.put("v", lastPingSentTime);
        room.send(message);
    }
}