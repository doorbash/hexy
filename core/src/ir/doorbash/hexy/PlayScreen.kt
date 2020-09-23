package ir.doorbash.hexy

import com.badlogic.gdx.*
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.controllers.Controller
import com.badlogic.gdx.controllers.ControllerListener
import com.badlogic.gdx.controllers.Controllers
import com.badlogic.gdx.controllers.PovDirection
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.*
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.math.*
import com.badlogic.gdx.math.Interpolation.ElasticOut
import com.badlogic.gdx.utils.Timer
import com.crowni.gdx.rtllang.support.ArFont
import com.crowni.gdx.rtllang.support.ArUtils
import gnu.trove.impl.sync.TSynchronizedLongObjectMap
import gnu.trove.map.hash.TLongObjectHashMap
import io.colyseus.Client
import io.colyseus.Client.MatchMakeException
import io.colyseus.Room
import io.colyseus.serializer.schema.Schema
import ir.doorbash.hexy.model.*
import ir.doorbash.hexy.util.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.util.*

/**
 * Created by Milad Doorbash on 7/22/2019.
 */
class PlayScreen internal constructor() : ScreenAdapter() {
    private val batch: SpriteBatch?
    private val gameCamera: OrthographicCamera
    private val fixedCamera: OrthographicCamera
    private val guiCamera: OrthographicCamera
    private val trailTexture: Texture
    private val mainAtlas: TextureAtlas?
    private var fillAtlas: TextureAtlas? = null
    private val whiteHex: AtlasRegion
    private var fbo: FrameBuffer? = null
    private var tiles: Sprite? = null
    private val thumbstickBgSprite: Sprite
    private val thumbstickPadSprite: Sprite
    private val timeBg: Sprite
    private val youWillRspwnBg: Sprite
    private val playerProgressBar: Sprite
    private val playerProgressBarBest: Sprite
    private val statsBg: Sprite
    private val coin: Sprite
    private var freetypeGeneratorNoto: FreeTypeFontGenerator? = null
    private var freetypeGeneratorArial: FreeTypeFontGenerator? = null
    private var logFont: BitmapFont? = null
    private var usernameFont: BitmapFont? = null
    private var leaderboardFont: BitmapFont? = null
    private var statsFont: BitmapFont? = null
    private var timeFont: BitmapFont? = null
    private val timeText: GlyphLayout
    private val youWillRspwnText: GlyphLayout
    private val yourProgressText: GlyphLayout
    private val yourProgressBestText: GlyphLayout
    private val loadingAnimation: LoadingAnimation?
    private var captureSound: Sound? = null
    private var clickSound: Sound? = null
    private var hitSound: Sound? = null
    private var boostSound: Sound? = null
    private var coinSound: Sound? = null
    private var deathSound: Sound? = null

    /* **************************************** FIELDS *******************************************/
    private var leaderboardDrawAgain = false
    private var mouseIsDown = false
    private var isUpdating = false
    private val soundIsOn: Boolean
    private val graphicsHigh: Boolean
    private val deviceRotationAvailable: Boolean

    //    private boolean controllerConnected;
    private var correctPlayerPositionTime = CORRECT_PLAYER_POSITION_INTERVAL
    private var sendDirectionTime = SEND_DIRECTION_INTERVAL
    private var sendPingTime = SEND_PING_INTERVAL
    private var checkConnectionTime = -1
    private var controllerType = Constants.CONTROL_FLOATING
    private var screenWidth = 0
    private var screenHeight = 0
    private var currentPing = 0
    private val gameMode = GAME_MODE_FFA
    private var connectionState = CONNECTION_STATE_DISCONNECTED
    private val langCode: Int
    private var bottomYi = 0
    private var leftXi = 0
    private var sizeX = 0
    private var sizeY = 0
    private var coinValue: Int
    private var lastPingSentTime: Long = 0
    private var lastPingReplyTime: Long = 0
    private var timeDiff: Long = 0
    private var lastDirection = -1000f
    private var direction = -1000f
    private var onScreenPadCurrentLen = 0f
    private var onScreenPadReleaseTimer = 0f
    private var onScreenPadInitLen = 0f
    private var progressbarWidth = 0f
    private var progressbarHeight = 0f
    private var progressbarTopMargin = 0f
    private var progressbarGap = 0f
    private var progressbarInitWidth = 0f
    private var yourProgressbarInitWidth = 0f
    private var progressbarExtraGapForCurrentPlayer = 0f
    private var guiUnits = 0f
    private var yourProgressbarWidth = 0f
    private val playerBestProgress = 0.448f
    private var time = 0f
    private var actualWidth = 0f
    private var actualHeight = 0f
    private var leftX = 0f
    private var bottomY = 0f
    private var leaderboardChangeSpeed = 0f
    private var sessionId: String? = null
    private var roomId: String? = null
    private val controllerAxis = FloatArray(2)
    private val rotationMatrix = FloatArray(4 * 4)
    private val leaderboardList: MutableList<Player> = ArrayList()
    private val players = TSynchronizedLongObjectMap(TLongObjectHashMap<Player?>(), playersMutex)
    private val drawList: MutableList<Long> = ArrayList()
    private val leaderboardDrawList: MutableList<FontDrawItem> = ArrayList()
    private val cells = Array(2 * MAP_SIZE + 1) { arrayOfNulls<Cell>(2 * MAP_SIZE + 1) }
    private val pathCells = Array(2 * MAP_SIZE + 1) { arrayOfNulls<Cell>(2 * MAP_SIZE + 1) }
    private val items = hashMapOf<String, Item>()
    private val textFadeOutAnimations: MutableList<TextFadeOutAnimation> = ArrayList()
    private val padAnchorPoint = Vector2()
    private val padVector = Vector2()
    private val onScreenPadPosition = Vector2()
    private var onScreenPadNorVector = Vector2()
    private val deathPosition = Vector2()
    private var connectionJob: Job? = null

    //    private Client client;
    private var room: Room<MyState>? = null
    private val arFont = ArFont()
    private var currentPlayer: Player? = null
    private val prefs: Preferences

    /* *************************************** OVERRIDE *****************************************/
    override fun render(dt: Float) {
        time += dt

        //Gdx.gl.glClearColor(0f, 0f, 0f, 1);
        //Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
//        Gdx.gl20.glEnable(GL20.GL_BLEND);
//        Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        if (room != null) {
            currentPlayer = room!!.state.players[room!!.sessionId]
            if (connectionState < CONNECTION_STATE_CLOSED) {
                if (currentPlayer != null && currentPlayer!!._stroke != null) {
//                camera.position.x = player.bc.getX() + player.bc.getWidth() / 2f;
//                camera.position.y = player.bc.getY() + player.bc.getHeight() / 2f;
                    gameCamera.position.x = MathUtils.lerp(gameCamera.position.x, currentPlayer!!._stroke!!.x + currentPlayer!!._stroke!!.width / 2f, CAMERA_LERP)
                    gameCamera.position.y = MathUtils.lerp(gameCamera.position.y, currentPlayer!!._stroke!!.y + currentPlayer!!._stroke!!.height / 2f, CAMERA_LERP)
                    if (ROTATE_CAMERA) {
                        val camAngle = -_Math.getCameraCurrentXYAngle(gameCamera)
                        var playerAngle = currentPlayer!!._angle * MathUtils.radiansToDegrees - 90
                        while (playerAngle < 0) playerAngle += 360f
                        while (playerAngle >= 360) playerAngle -= 360f
                        gameCamera.rotate(camAngle - playerAngle)
                    }
                }
            } else {
                if (currentPlayer != null && currentPlayer!!._stroke != null) {
                    gameCamera.position.x = MathUtils.lerp(gameCamera.position.x, deathPosition.x, CAMERA_DEATH_LERP)
                    gameCamera.position.y = MathUtils.lerp(gameCamera.position.y, deathPosition.y, CAMERA_DEATH_LERP)
                }
            }
        }
        gameCamera.update()
        actualWidth = gameCamera.zoom * gameCamera.viewportWidth
        actualHeight = gameCamera.zoom * gameCamera.viewportHeight
        leftX = gameCamera.position.x - actualWidth / 2f
        bottomY = gameCamera.position.y - actualHeight / 2f
        bottomYi = Math.floor((bottomY + GRID_HEIGHT / 2f) / GRID_HEIGHT.toDouble()).toInt() - 1
        leftXi = (if (bottomYi % 2 == 0) Math.floor((leftX + GRID_WIDTH / 2f) / GRID_WIDTH.toDouble()) else Math.floor(leftX / GRID_WIDTH.toDouble())).toInt() - 1
        sizeX = (actualWidth / GRID_WIDTH).toInt() + 3
        sizeY = (actualHeight / GRID_HEIGHT).toInt() + 3
        drawList.clear()
        batch!!.projectionMatrix = gameCamera.combined
        batch.begin()
        batch.disableBlending()
        drawTiles()
        batch.enableBlending()
        batch.flush()
        if (room != null && (connectionState == CONNECTION_STATE_CONNECTED && !isUpdating || connectionState == CONNECTION_STATE_CLOSED)) {
            if (connectionState == CONNECTION_STATE_CONNECTED) {
                updatePlayersPositions(dt)
                updateZoom()
            }
            drawCells()
            if (graphicsHigh) drawTextureCells()
            batch.end()
            drawTrails()
            batch.begin()
            drawItems()
            drawPlayers()
            drawPlayerFillTextures()
            drawPlayerNames()
            drawTextFadeOutAnimations(dt)
        }
        batch.projectionMatrix = fixedCamera.combined
        if ((controllerType == Constants.CONTROL_FIXED_LEFT || controllerType == Constants.CONTROL_FIXED_RIGHT) && !mouseIsDown && !MathUtils.isEqual(onScreenPadCurrentLen, 0f)) {
//            System.out.println("bouncing....");
            onScreenPadReleaseTimer += dt
            onScreenPadCurrentLen = (1 - ON_SCREEN_PAD_RELEASE_ELASTIC_OUT.apply(Math.min(1f, onScreenPadReleaseTimer / ON_SCREEN_PAD_RELEASE_TOTAL_TIME))) * onScreenPadInitLen
            //            System.out.println("current pad length is " + onScreenPadCurrentLen);
            padVector[onScreenPadNorVector.x * onScreenPadCurrentLen] = onScreenPadNorVector.y * onScreenPadCurrentLen
            thumbstickPadSprite.setCenter(onScreenPadPosition.x + padVector.x, onScreenPadPosition.y + padVector.y)
        }
        if (controllerType == Constants.CONTROL_FLOATING && mouseIsDown || controllerType == Constants.CONTROL_FIXED_LEFT || controllerType == Constants.CONTROL_FIXED_RIGHT) {
            thumbstickBgSprite.draw(batch)
            thumbstickPadSprite.draw(batch)
        }
        batch.projectionMatrix = guiCamera.combined
        if (connectionState == CONNECTION_STATE_CONNECTED && room != null && room!!.state.started) {
            leaderboardDrawList.clear()
            drawLeaderboard(dt)
            if (gameMode == GAME_MODE_BATTLE && !room!!.state.ended) {
                drawTime()
                drawYouWillRespawnText()
            } else if (gameMode == GAME_MODE_FFA) {
//                drawCoin();
                drawStatsBackground()
                drawPlayerProgress()
                //                drawCoinText();
                drawStatsText()
            }
            for (fdi in leaderboardDrawList) {
                leaderboardFont!!.color = fdi.color
                leaderboardFont!!.draw(batch, fdi.text, fdi.x, fdi.y)
            }
        }
        if (connectionState == CONNECTION_STATE_CONNECTED && isUpdating || connectionState == CONNECTION_STATE_CONNECTING || connectionState == CONNECTION_STATE_DISCONNECTED) {
            drawConnecting(dt)
        } else {
//            drawKills();
            drawPing()
            //            connectTime += dt;
        }
        batch.end()
        if (sendDirectionTime < 0) {
            sendDirection()
            sendDirectionTime = SEND_DIRECTION_INTERVAL
        } else sendDirectionTime -= (dt * 1000).toInt()
        if (sendPingTime < 0) {
            sendPing()
            sendPingTime = SEND_PING_INTERVAL
        } else sendPingTime -= (dt * 1000).toInt()
        if (checkConnectionTime < 0) {
            checkConnection()
            checkConnectionTime = CHECK_CONNECTION_INTERVAL
        } else checkConnectionTime -= (dt * 1000).toInt()
        if (controllerType == Constants.CONTROL_DEVICE_ROTATION && deviceRotationAvailable) {
            Gdx.input.getRotationMatrix(rotationMatrix)
            val m = Matrix4(rotationMatrix)
            val q = m.getRotation(Quaternion())
            direction = Math.toDegrees(Math.atan2(q.pitch.toDouble(), -q.yaw.toDouble())).toFloat()
            //            System.out.println("pitch = " + q.getPitch());
//            System.out.println("yaw = " + -q.getYaw());
//            System.out.println(";;;;;;;;;;;;;;;;");
        }

        // game controller
        if ( /*controllerConnected && */Math.abs(controllerAxis[0]) > 0.1f || Math.abs(controllerAxis[1]) > 0.1f) {
            direction = Math.toDegrees(Math.atan2((-controllerAxis[0]).toDouble(), controllerAxis[1].toDouble())).toFloat()
        }

//        System.out.println(";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;");
    }

    override fun resize(width: Int, height: Int) {
        if (controllerType == Constants.CONTROL_DEVICE_ROTATION) {
            //super.resize(width, height);
            return
        }
        println("resize($width, $height)")
        init(width, height)
        gameCamera.viewportWidth = screenWidth.toFloat()
        gameCamera.viewportHeight = screenHeight.toFloat()
        gameCamera.update()
        fixedCamera.viewportWidth = screenWidth.toFloat()
        fixedCamera.viewportHeight = screenHeight.toFloat()
        fixedCamera.update()
        guiCamera.viewportWidth = width.toFloat()
        guiCamera.viewportHeight = height.toFloat()
        guiCamera.update()
        updateGuiValues()
        leaderboardDrawAgain = true
    }

    override fun dispose() {
        room?.leave()
        batch?.dispose()
        fbo?.dispose()
        mainAtlas?.dispose()
        fillAtlas?.dispose()
        logFont?.dispose()
        usernameFont?.dispose()
        leaderboardFont?.dispose()
        statsFont?.dispose()
        timeFont?.dispose()
        freetypeGeneratorNoto?.dispose()
        freetypeGeneratorArial?.dispose()
        loadingAnimation?.dispose()
        captureSound?.dispose()
        boostSound?.dispose()
        coinSound?.dispose()
        hitSound?.dispose()
        deathSound?.dispose()
        clickSound?.dispose()
        connectionJob?.cancel()
    }

    /* ***************************************** INIT *******************************************/
    private fun init(width: Int, height: Int) {
        screenWidth = if (height > width) SCREEN_WIDTH_PORTRAIT else SCREEN_WIDTH_LANDSCAPE
        screenHeight = screenWidth * height / width
        leaderboardChangeSpeed = LEADERBORAD_CHANGE_SPEED * height / screenHeight
        if (controllerType == Constants.CONTROL_FIXED_RIGHT) {
            onScreenPadPosition[screenWidth / 2f - 120] = -screenHeight / 2f + 120
        } else {
            onScreenPadPosition[-screenWidth / 2f + 120] = -screenHeight / 2f + 120
        }
        if (controllerType == Constants.CONTROL_FIXED_LEFT || controllerType == Constants.CONTROL_FIXED_RIGHT) {
            thumbstickBgSprite.setCenter(onScreenPadPosition.x, onScreenPadPosition.y)
            thumbstickPadSprite.setCenter(onScreenPadPosition.x, onScreenPadPosition.y)
        }
    }

    private fun initFonts() {
        freetypeGeneratorNoto = FreeTypeFontGenerator(Gdx.files.internal(PATH_FONT_NOTO))
        freetypeGeneratorArial = FreeTypeFontGenerator(Gdx.files.internal(PATH_FONT_ARIAL))
        val logFontParams = FreeTypeFontParameter()
        logFontParams.size = 14 * Gdx.graphics.width / screenWidth
        logFontParams.color = Color.BLACK
        logFontParams.flip = false
        logFontParams.incremental = true
        logFont = freetypeGeneratorNoto!!.generateFont(logFontParams)
        val leaderboardFontParams = FreeTypeFontParameter()
        leaderboardFontParams.characters += ArUtils.getAllChars().toString("")
        leaderboardFontParams.size = 16 * Gdx.graphics.width / screenWidth
        leaderboardFontParams.color = Color(0.8f, 0.8f, 0.8f, 1.0f)
        leaderboardFontParams.flip = false
        leaderboardFontParams.incremental = true
        leaderboardFontParams.magFilter = Texture.TextureFilter.Linear
        leaderboardFontParams.minFilter = leaderboardFontParams.magFilter
        leaderboardFont = freetypeGeneratorArial!!.generateFont(leaderboardFontParams)
        val statsFontParams = FreeTypeFontParameter()
        statsFontParams.characters += ArUtils.getAllChars().toString("")
        statsFontParams.size = 12 * Gdx.graphics.width / screenWidth
        statsFontParams.color = Color(1f, 1f, 1f, 1.0f)
        statsFontParams.flip = false
        statsFontParams.incremental = true
        statsFontParams.magFilter = Texture.TextureFilter.Linear
        statsFontParams.minFilter = statsFontParams.magFilter
        statsFont = freetypeGeneratorArial!!.generateFont(statsFontParams)
        val usernameFontParams = FreeTypeFontParameter()
        usernameFontParams.characters += ArUtils.getAllChars().toString("")
        usernameFontParams.size = 16
        usernameFontParams.color = Color(0.8f, 0.8f, 0.8f, 1.0f)
        usernameFontParams.flip = false
        usernameFontParams.incremental = true
        usernameFontParams.magFilter = Texture.TextureFilter.Linear
        usernameFontParams.minFilter = usernameFontParams.magFilter
        usernameFont = freetypeGeneratorArial!!.generateFont(usernameFontParams)
        val timeFontParams = FreeTypeFontParameter()
        timeFontParams.characters += ArUtils.getAllChars().toString("")
        timeFontParams.size = 20 * Gdx.graphics.width / screenWidth
        timeFontParams.color = Color(1f, 1f, 1f, 1f)
        timeFontParams.flip = false
        timeFontParams.incremental = true
        timeFontParams.magFilter = Texture.TextureFilter.Linear
        timeFontParams.minFilter = timeFontParams.magFilter
        timeFont = freetypeGeneratorArial!!.generateFont(timeFontParams)
    }

    private fun initTiles() {
        fbo = FrameBuffer(Pixmap.Format.RGBA8888, 2 * SCREEN_WIDTH_LANDSCAPE, 2 * SCREEN_WIDTH_LANDSCAPE, false)
        fbo!!.begin()
        Gdx.gl.glClearColor(0.92f, 0.92f, 0.92f, 1.0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        val m = Matrix4()
        m.setToOrtho2D(0f, 0f, fbo!!.width.toFloat(), fbo!!.height.toFloat())
        batch!!.projectionMatrix = m
        batch.begin()
        //        mainBatch.disableBlending();
        for (xi in -3..36) {
            for (yi in -3..42) {
                batch.draw(whiteHex, xi * GRID_WIDTH + (if (yi % 2 == 0) 0f else GRID_WIDTH / 2f) - CELL_WIDTH / 2f, yi * GRID_HEIGHT - CELL_HEIGHT / 2f, CELL_WIDTH, CELL_HEIGHT)
            }
        }

//        mainBatch.enableBlending();
        batch.end()
        fbo!!.end()
        tiles = Sprite(fbo!!.colorBufferTexture)
        tiles!!.flip(false, true)
    }

    private fun initInput() {
        Gdx.input.inputProcessor = object : InputProcessor {
            var leftIsDown = false
            var rightIsDown = false
            var upIsDown = false
            var downIsDown = false
            private fun updateAxis() {
                if (rightIsDown) {
                    controllerAxis[1] = 1f
                } else if (leftIsDown) {
                    controllerAxis[1] = (-1).toFloat()
                } else {
                    controllerAxis[1] = 0f
                }
                if (upIsDown) {
                    controllerAxis[0] = (-1).toFloat()
                } else if (downIsDown) {
                    controllerAxis[0] = 1f
                } else {
                    controllerAxis[0] = 0f
                }
            }

            override fun keyDown(keycode: Int): Boolean {
                if (keycode == Input.Keys.BACK) {
                    if (room != null) room!!.leave()
                    Gdx.app.exit()
                    return true
                } else if (keycode == Input.Keys.RIGHT) {
                    rightIsDown = true
                    updateAxis()
                    return true
                } else if (keycode == Input.Keys.LEFT) {
                    leftIsDown = true
                    updateAxis()
                    return true
                } else if (keycode == Input.Keys.UP) {
                    upIsDown = true
                    updateAxis()
                    return true
                } else if (keycode == Input.Keys.DOWN) {
                    downIsDown = true
                    updateAxis()
                    return true
                }
                return false
            }

            override fun keyUp(keycode: Int): Boolean {
                if (keycode == Input.Keys.RIGHT) {
                    rightIsDown = false
                    updateAxis()
                    return true
                } else if (keycode == Input.Keys.LEFT) {
                    leftIsDown = false
                    updateAxis()
                    return true
                } else if (keycode == Input.Keys.UP) {
                    upIsDown = false
                    updateAxis()
                    return true
                } else if (keycode == Input.Keys.DOWN) {
                    downIsDown = false
                    updateAxis()
                    return true
                }
                return false
            }

            override fun keyTyped(character: Char): Boolean {
                return false
            }

            override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
//                System.out.println("TouchDown");
                var screenX = screenX
                var screenY = screenY
                mouseIsDown = true
                screenX = ((screenX - Gdx.graphics.width / 2f) * screenWidth / Gdx.graphics.width).toInt()
                screenY = ((Gdx.graphics.height / 2f - screenY) * screenHeight / Gdx.graphics.height).toInt()
                padAnchorPoint[screenX.toFloat()] = screenY.toFloat()
                handleTouchDownDrag(screenX, screenY)
                return true
            }

            override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
//                System.out.println("TouchUp");
                mouseIsDown = false
                return true
            }

            override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
//                System.out.println("touchDragged()");
                var screenX = screenX
                var screenY = screenY
                screenX = ((screenX - Gdx.graphics.width / 2f) * fixedCamera.viewportWidth / Gdx.graphics.width).toInt()
                screenY = ((Gdx.graphics.height / 2f - screenY) * fixedCamera.viewportHeight / Gdx.graphics.height).toInt()
                handleTouchDownDrag(screenX, screenY)
                return true
            }

            override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
                return false
            }

            override fun scrolled(amount: Int): Boolean {
                return false
            }
        }
    }

    private fun initControllers() {
        println("Controllers: " + Controllers.getControllers().size)
        var i = 0
        for (controller in Controllers.getControllers()) {
            println("#" + i++ + ": " + controller.name)
        }
        if (Controllers.getControllers().size == 0) println("No controllers attached")
        //        else controllerConnected = true;

        // setup the listener that prints events to the console
        Controllers.addListener(object : ControllerListener {
            fun indexOf(controller: Controller?): Int {
                return Controllers.getControllers().indexOf(controller, true)
            }

            override fun connected(controller: Controller) {
                println("connected " + controller.name)
                var i = 0
                for (c in Controllers.getControllers()) {
                    println("#" + i++ + ": " + c.name)
                }
            }

            override fun disconnected(controller: Controller) {
                println("disconnected " + controller.name)
                var i = 0
                for (c in Controllers.getControllers()) {
                    println("#" + i++ + ": " + c.name)
                }
                if (Controllers.getControllers().size == 0) println("No controllers attached")
            }

            override fun buttonDown(controller: Controller, buttonIndex: Int): Boolean {
                println("#" + indexOf(controller) + ", button " + buttonIndex + " down")
                return false
            }

            override fun buttonUp(controller: Controller, buttonIndex: Int): Boolean {
                println("#" + indexOf(controller) + ", button " + buttonIndex + " up")
                return false
            }

            override fun axisMoved(controller: Controller, axisIndex: Int, value: Float): Boolean {
                println("#" + indexOf(controller) + ", axis " + axisIndex + ": " + value)
                if (axisIndex == 0) controllerAxis[0] = value else if (axisIndex == 1) controllerAxis[1] = value
                return false
            }

            override fun povMoved(controller: Controller, povIndex: Int, value: PovDirection): Boolean {
                println("#" + indexOf(controller) + ", pov " + povIndex + ": " + value)
                return false
            }

            override fun xSliderMoved(controller: Controller, sliderIndex: Int, value: Boolean): Boolean {
                println("#" + indexOf(controller) + ", x slider " + sliderIndex + ": " + value)
                return false
            }

            override fun ySliderMoved(controller: Controller, sliderIndex: Int, value: Boolean): Boolean {
                println("#" + indexOf(controller) + ", y slider " + sliderIndex + ": " + value)
                return false
            }

            override fun accelerometerMoved(controller: Controller, accelerometerIndex: Int, value: Vector3): Boolean {
                // not printing this as we get to many values
                return false
            }
        })
    }

    private fun initCellSprite(player: Player, cell: Cell?) {
        if (cell == null) return
        if (players[cell.pid] == null) return
        if (player.fillColor == null) {
            if (graphicsHigh) cell.textureSprite = fillAtlas!!.createSprite(player.fill)
            if (cell.textureSprite != null) {
                cell.textureSprite!!.setSize(CELL_TEX_WIDTH, CELL_TEX_WIDTH)
                val pos = getHexPosition(cell.x.toInt(), cell.y.toInt())
                cell.textureSprite!!.setCenter(pos.x, pos.y /* - (CELL_HEIGHT - CELL_WIDTH) / 2f*/)
            }
        } else {
            cell.textureSprite = null
        }
        cell.colorSprite = mainAtlas!!.createSprite(TEXTURE_REGION_HEX_WHITE)
        cell.colorSprite?.setColor(players[cell.pid]!!.strokeColor)
        cell.colorSprite?.setSize(CELL_WIDTH, CELL_HEIGHT)
        val pos = getHexPosition(cell.x.toInt(), cell.y.toInt())
        cell.colorSprite?.setCenter(pos.x, pos.y)
    }

    private fun initPathCellSprite(player: Player?, pathCell: Cell?) {
        if (pathCell == null) return
        if (player == null) return
        if (players[pathCell.pid] == null) return
        pathCell.colorSprite = mainAtlas!!.createSprite(TEXTURE_REGION_HEX_WHITE)
        pathCell.colorSprite?.setColor(players[pathCell.pid]!!.pathCellColor)
        pathCell.colorSprite?.setSize(CELL_WIDTH, CELL_HEIGHT)
        val pos = getHexPosition(pathCell.x.toInt(), pathCell.y.toInt())
        pathCell.colorSprite?.setCenter(pos.x, pos.y)
    }

    /* ***************************************** DRAW *******************************************/ //    private void drawConnecting(float dt) {
    //        float totalWidth = connectingText.width + LOADING_ANIMATION_SIZE + 16;
    //        loadingAnimation.render(dt, batch, -totalWidth / 2f, -LOADING_ANIMATION_SIZE / 2f, LOADING_ANIMATION_SIZE, LOADING_ANIMATION_SIZE);
    //        float alpha = (MathUtils.sin(4 * time) + 1) / 2f;
    //        usernameFont.setColor(new Color(CONNECTING_TEXT_COLOR.r, CONNECTING_TEXT_COLOR.b, CONNECTING_TEXT_COLOR.g, alpha));
    //        usernameFont.draw(batch, "Connecting...", -totalWidth / 2f + LOADING_ANIMATION_SIZE + 16, usernameFont.getLineHeight() / 2f - 4);
    //    }
    private fun drawConnecting(dt: Float) {
        val size = 20 * guiUnits
        val gap = 8 * guiUnits
        val x = -guiCamera.viewportWidth / 2f + 2 * guiUnits
        val y = -guiCamera.viewportHeight / 2f + 2 * guiUnits
        loadingAnimation!!.render(dt, batch, x, y, size, size)
        val alpha = (MathUtils.sin(4 * time) + 1) / 2f
        leaderboardFont!!.color = Color(CONNECTING_TEXT_COLOR.r, CONNECTING_TEXT_COLOR.b, CONNECTING_TEXT_COLOR.g, alpha)
        // TODO: inja yebar lango en kardam null pointer dad
        leaderboardFont!!.draw(batch, arFont.getText(I18N.texts[langCode][I18N.connecting]), x + size + gap, y + size / 2f + leaderboardFont!!.lineHeight / 2f - 4 * guiUnits)
    }

    private fun drawTiles() {
        val firstX: Float = leftXi * GRID_WIDTH + if (bottomYi % 2 == 0) 0f else GRID_WIDTH / 2f
        val firstY = bottomYi * GRID_HEIGHT
        tiles!!.x = firstX
        tiles!!.y = firstY
        tiles!!.draw(batch)
    }

    private fun drawTrails() {
        for (drawPid in drawList) {
            val player = players[drawPid] ?: continue
            if (player.status.toInt() == 0 && player.trailGraphic != null) {
                player.trailGraphic!!.render(gameCamera.combined)
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

    private fun drawCells() {
        for (x in leftXi..leftXi + sizeX) {
            if (x < -MAP_SIZE || x > MAP_SIZE) continue
            for (y in bottomYi..bottomYi + sizeY) {
                if (y < -MAP_SIZE || y > MAP_SIZE) continue
                val cell = cells[x + MAP_SIZE][y + MAP_SIZE]
                val pathCell = pathCells[x + MAP_SIZE][y + MAP_SIZE]
                val hasCell = cell?.colorSprite != null
                val hasPathCell = pathCell?.colorSprite != null
                if (hasCell && hasPathCell) {
                    if (cell!!.pid == pathCell!!.pid) {
                        // only draw cell
                        if (players[cell.pid] == null) continue
                        cell.colorSprite!!.draw(batch)
                        //                        if (DEBUG_DRAW_PIDS)
//                            usernameFont.draw(batch, cell.pid + "", cell.sprite.getX() + cell.sprite.getWidth() / 2f, cell.sprite.getY() + cell.sprite.getHeight() / 2f);
                    } else {
                        // only draw path cell
                        if (players[pathCell.pid] == null) continue
                        pathCell.colorSprite!!.draw(batch)
                        //                        if (DEBUG_DRAW_PIDS)
//                            usernameFont.draw(batch, pathCell.pid + "", pathCell.sprite.getX() + pathCell.sprite.getWidth() / 2f, pathCell.sprite.getY() + pathCell.sprite.getHeight() / 2f);
                    }
                    if (!drawList.contains(cell.pid)) drawList.add(cell.pid)
                    if (!drawList.contains(pathCell.pid)) drawList.add(pathCell.pid)
                } else if (hasCell) {
                    // draw cell
                    if (players[cell!!.pid] == null) continue
                    cell.colorSprite!!.draw(batch)
                    //                    if (DEBUG_DRAW_PIDS)
//                        usernameFont.draw(batch, cell.pid + "", cell.sprite.getX() + cell.sprite.getWidth() / 2f, cell.sprite.getY() + cell.sprite.getHeight() / 2f);
                    if (!drawList.contains(cell.pid)) drawList.add(cell.pid)
                } else if (hasPathCell) {
                    // draw path cell
                    if (players[pathCell!!.pid] == null) continue
                    pathCell.colorSprite!!.draw(batch)
                    //                    if (DEBUG_DRAW_PIDS)
//                        usernameFont.draw(batch, pathCell.pid + "", pathCell.sprite.getX() + pathCell.sprite.getWidth() / 2f, pathCell.sprite.getY() + pathCell.sprite.getHeight() / 2f);
                    if (!drawList.contains(pathCell.pid)) drawList.add(pathCell.pid)
                }
            }
        }
    }

    private fun drawTextureCells() {
        for (x in leftXi..leftXi + sizeX) {
            if (x < -MAP_SIZE || x > MAP_SIZE) continue
            for (y in bottomYi..bottomYi + sizeY) {
                if (y < -MAP_SIZE || y > MAP_SIZE) continue
                val cell = cells[x + MAP_SIZE][y + MAP_SIZE]
                if (cell == null || cell.colorSprite == null || cell.textureSprite == null || players[cell.pid] == null) continue
                val pathCell = pathCells[x + MAP_SIZE][y + MAP_SIZE]
                val hasPathCell = pathCell != null && pathCell.colorSprite != null
                if (hasPathCell) {
                    if (cell.pid == pathCell!!.pid) cell.textureSprite!!.draw(batch)
                } else {
                    cell.textureSprite!!.draw(batch)
                }
            }
        }
    }

    private fun drawPlayers() {
        for (drawPid in drawList) {
            val player = players[drawPid] ?: continue
            if (player.status.toInt() == 0) {
                if (DEBUG_SHOW_GHOST && player.bcGhost != null) player.bcGhost!!.draw(batch)
                if (player._stroke != null) player._stroke!!.draw(batch)
                if (player._fill != null && !player.fillIsTexture) player._fill!!.draw(batch)
                if (player.indic != null) player.indic!!.draw(batch)
            }
        }
    }

    private fun drawPlayerFillTextures() {
        for (drawPid in drawList) {
            val player = players[drawPid] ?: continue
            if (player.status.toInt() == 0) {
                if (player._fill != null && player.fillIsTexture) player._fill!!.draw(batch)
            }
        }
    }

    private fun drawPlayerNames() {
        for (drawPid in drawList) {
            val player = players[drawPid] ?: continue
            if (player.status.toInt() == 0) {
                if (player._stroke != null && player.text != null) {
                    val x = player._stroke!!.x + player._stroke!!.width / 2f - player.text!!.width / 2f
                    val y = player._stroke!!.y + 70
                    usernameFont!!.color = player.strokeColor
                    usernameFont!!.draw(batch, player._name, x, y)
                }
            }
        }
    }

    private fun drawItems() {
        synchronized(items) {
            for (item in items.values) {
                if (item.sprite == null) continue
                if (item.x >= leftX && item.x <= leftX + actualWidth && item.y >= bottomY && item.y <= bottomY + actualHeight) {
                    item.sprite!!.draw(batch)
                }
                val x = MathUtils.lerp(item.sprite!!.x + item.sprite!!.width / 2f, item.x, 0.4f)
                val y = MathUtils.lerp(item.sprite!!.y + item.sprite!!.height / 2f, item.y, 0.4f)
                item.sprite!!.setCenter(x, y)
            }
        }
    }

    private fun drawTextFadeOutAnimations(dt: Float) {
        synchronized(textFadeOutAnimations) {
            val removeList: MutableList<TextFadeOutAnimation> = ArrayList()
            for (tfo in textFadeOutAnimations) {
                if (tfo.stop) removeList.add(tfo) else tfo.draw(batch, dt, usernameFont)
            }
            for (tfo in removeList) {
                textFadeOutAnimations.remove(tfo)
            }
        }
    }

    private fun drawProgressbar(player: Player, dt: Float, drawStatic: Boolean) {
        val decimalFormat = DecimalFormat("#0.0")
        val totalWidth = progressbarWidth - progressbarInitWidth
        val percentage = player.numCells / TOTAL_CELLS.toFloat()
        player._percentage = MathUtils.lerp(player._percentage, percentage, 0.04f)
        val width = player._percentage * totalWidth + progressbarInitWidth
        player.progressBar!!.setSize(progressbarWidth, progressbarHeight)
        player.progressBar!!.x = guiCamera.viewportWidth / 2f - width
        val y: Float
        y = if (drawStatic && player._position > LEADERBOARD_NUM + 1) {
            guiCamera.viewportHeight / 2f - progressbarTopMargin - LEADERBOARD_NUM * (progressbarHeight + progressbarGap) - progressbarHeight - progressbarExtraGapForCurrentPlayer
        } else {
            guiCamera.viewportHeight / 2f - progressbarTopMargin - (player._position - 1) * (progressbarHeight + progressbarGap) - progressbarHeight
        }
        if (drawStatic || !player.positionIsChanging) {
            player.progressBar!!.y = y
        } else {
            if (player.progressBar!!.y < y) {
                player.progressBar!!.translateY(dt * leaderboardChangeSpeed)
                if (player.progressBar!!.y > y) {
                    player.progressBar!!.y = y
                    player.positionIsChanging = false
                }
            } else {
                player.progressBar!!.translateY(-dt * leaderboardChangeSpeed)
                if (player.progressBar!!.y < y) {
                    player.progressBar!!.y = y
                    player.positionIsChanging = false
                }
            }
        }
        player.progressBar!!.draw(batch)
        if (player.positionIsChanging && player.changeDir == Player.CHANGE_DIRECTION_DOWN) return
        val fdi = FontDrawItem()
        fdi.color = player.strokeColor
        fdi.text = "${player._position}- ${decimalFormat.format(percentage * 100f.toDouble())}% ${player._name}"
        fdi.x = player.progressBar!!.x + 6 * guiUnits
        fdi.y = player.progressBar!!.y + (progressbarHeight + leaderboardFont!!.lineHeight) / 2f - 2 * guiUnits
        leaderboardDrawList.add(fdi)
    }

    private fun drawLeaderboard(dt: Float) {
        synchronized(leaderboardList) {


            // sort players by numCells
            Collections.sort(leaderboardList, SORT_PLAYERS_BY_NUM_CELLS)
            var kk: Short = 1
            for (cm in leaderboardList) {
                cm.position = kk++.toInt()
            }
            Collections.sort(leaderboardList, SORT_PLAYERS_BY_POSITION)
            if (!leaderboardDrawAgain) {
                val _positions: MutableList<Int> = ArrayList()
                var allWrong = false
                for (player in leaderboardList) {
                    if (_positions.contains(player._position)) {
                        allWrong = true
                        break
                    }
                    _positions.add(player._position)
                }
                if (allWrong) {
                    var x = 1
                    for (player in leaderboardList) {
                        player._position = x++
                    }
                    leaderboardDrawAgain = true
                }
            }
            if (leaderboardDrawAgain) {
                for (player in leaderboardList) {
                    player.positionIsChanging = false
                    if (player.progressBar != null) {
                        player.progressBar!!.x = Gdx.graphics.width / 2f - (player._percentage * (progressbarWidth - progressbarInitWidth) + progressbarInitWidth)
                        player.progressBar!!.y = Gdx.graphics.height / 2f - progressbarTopMargin - Math.min(player._position - 1, LEADERBOARD_NUM) * (progressbarHeight + progressbarGap) - progressbarHeight
                    }
                }
                leaderboardDrawAgain = false
            } else {
                var i = 0
                while (i < leaderboardList.size - 1) {
                    val player = leaderboardList[i]
                    if (player.positionIsChanging) {
                        i++
                        continue
                    }
                    if (player.position > player._position) { // yani bayad bere paeen
                        val next = leaderboardList[i + 1]
                        if (!next.positionIsChanging && next.position <= next._position) {
                            if (player._position <= LEADERBOARD_NUM) {
                                if (next._position <= LEADERBOARD_NUM + 1) {
                                    player.positionIsChanging = true
                                    player.changeDir = Player.CHANGE_DIRECTION_DOWN
                                    next.positionIsChanging = true
                                    next.changeDir = Player.CHANGE_DIRECTION_UP
                                }
                            }
                            player._position++
                            next._position--
                            i++
                        }
                    }
                    i++
                }
                Collections.sort(leaderboardList, SORT_PLAYERS_BY_POSITION)
            }
            var playerProgressPrinted = false
            //            Player currentPlayer = room.state.players.get(room.getSessionId());
            val limit = Math.min(LEADERBOARD_NUM + 1, leaderboardList.size)
            for (i in limit - 1 downTo 0) {
                val player = leaderboardList[i] // _position = i + 1
                if (player == null || player.progressBar == null) continue
                if (i == LEADERBOARD_NUM) {
                    if (!player.positionIsChanging) continue
                    if (player.changeDir == Player.CHANGE_DIRECTION_UP) continue
                }
                if (players[player.pid] == null) {
                    player.positionIsChanging = false
                    continue
                }
                drawProgressbar(player, dt, false)
                if (currentPlayer != null && currentPlayer!!.pid == player.pid) playerProgressPrinted = true
            }
            if (!playerProgressPrinted) {
                if (currentPlayer == null || currentPlayer!!.progressBar == null) return
                drawProgressbar(currentPlayer!!, dt, true)
            }
        }
    }

    private fun drawPlayerProgress() {
//        Player currentPlayer = room.state.players.get(room.getSessionId());
        if (currentPlayer == null || currentPlayer!!.strokeColor == null) return
        val decimalFormat = DecimalFormat("#0.0")
        val totalWidth = yourProgressbarWidth - yourProgressbarInitWidth
        playerProgressBarBest.setSize(playerBestProgress * totalWidth + yourProgressbarInitWidth, progressbarHeight)
        playerProgressBarBest.x = -guiCamera.viewportWidth / 2f
        playerProgressBarBest.y = guiCamera.viewportHeight / 2f - progressbarTopMargin - progressbarHeight
        playerProgressBar.setSize(currentPlayer!!._percentage * totalWidth + yourProgressbarInitWidth, progressbarHeight)
        playerProgressBar.x = -guiCamera.viewportWidth / 2f
        playerProgressBar.y = guiCamera.viewportHeight / 2f - progressbarTopMargin - progressbarHeight
        playerProgressBarBest.draw(batch)
        playerProgressBar.draw(batch)
        var fdi = FontDrawItem()
        fdi.color = currentPlayer!!.strokeColor
        fdi.text = (if (currentPlayer!!._percentage < 0.1f) " " else "") + decimalFormat.format(currentPlayer!!._percentage * 100f.toDouble()) + "%"
        fdi.x = playerProgressBar.x + playerProgressBar.width - yourProgressText.width + guiUnits * 8
        fdi.y = playerProgressBar.y + (progressbarHeight + leaderboardFont!!.lineHeight) / 2f - 2 * guiUnits
        leaderboardDrawList.add(fdi)
        fdi = FontDrawItem()
        fdi.color = COLOR_YOUR_BEST_PROGRESS_TEXT
        fdi.text = "BEST " + decimalFormat.format(playerBestProgress * 100.toDouble()) + "%"
        fdi.x = playerProgressBarBest.x + playerProgressBarBest.width - yourProgressBestText.width + guiUnits * 8
        fdi.y = playerProgressBarBest.y - 2 * guiUnits
        leaderboardDrawList.add(fdi)
    }

    private fun drawCoin() {
        coin.setSize(progressbarHeight * 0.8f, progressbarHeight * 0.8f)
        coin.x = -Gdx.graphics.width / 2f + 8 * guiUnits
        coin.y = guiCamera.viewportHeight / 2f - 70 * guiUnits
        coin.draw(batch)
    }

    private fun drawCoinText() {
        leaderboardFont!!.color = COLOR_YOUR_BEST_PROGRESS_TEXT
        leaderboardFont!!.draw(batch, coinValue.toString() + "", -Gdx.graphics.width / 2f + coin.width + 15 * guiUnits, guiCamera.viewportHeight / 2f - 73 * guiUnits + leaderboardFont!!.lineHeight)
    }

    private fun drawTime() {
        val remainingTime = room!!.state.endTime - serverTime
        val seconds = (remainingTime / 1000).toInt() % 60
        val secondsText = (if (seconds < 10) "0" else "") + seconds
        val minutes = (remainingTime / 60000).toInt()
        val minutesText = (if (minutes < 10) "0" else "") + minutes
        val x = -Gdx.graphics.width / 2f + 10 * guiUnits
        val y = Gdx.graphics.height / 2f - 10 * guiUnits
        timeBg.setSize(timeText.width + 12 * guiUnits, timeText.height + 12 * guiUnits)
        timeBg.setPosition(x - 6 * guiUnits, y - 6 * guiUnits - timeText.height)
        timeBg.draw(batch)
        timeFont!!.draw(batch, "$minutesText:$secondsText", x, y)
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
    private fun drawYouWillRespawnText() {
//        Player player = room.state.players.get(room.getSessionId());
        if (currentPlayer == null || currentPlayer!!.status.toInt() != 1) return
        val remainingTime = (currentPlayer!!.rspwnTime - serverTime).toInt()
        val seconds = remainingTime / 1000
        if (seconds < 0 || seconds > 9) return
        val x = -youWillRspwnText.width / 2f
        val y = Gdx.graphics.height / 4f
        youWillRspwnBg.setSize(youWillRspwnText.width + 12 * guiUnits, youWillRspwnText.height + 12 * guiUnits)
        youWillRspwnBg.setPosition(x - 6 * guiUnits, y - youWillRspwnText.height - 6 * guiUnits)
        youWillRspwnBg.draw(batch)
        timeFont!!.draw(batch, "You will respawn in $seconds seconds", x, y)
    }

    private fun drawPing() {
        var logText = "fps: " + Gdx.graphics.framesPerSecond
        if (currentPing > 0) {
            logText += " - ping: $currentPing"
        }
        logFont!!.draw(batch, logText, -Gdx.graphics.width / 2f + 8 * guiUnits, -Gdx.graphics.height / 2f + 2 * guiUnits + logFont!!.lineHeight)
    }

    private fun drawStatsBackground() {
        statsBg.setSize(70 * guiUnits, progressbarHeight)
        statsBg.setPosition(-guiCamera.viewportWidth / 2f, guiCamera.viewportHeight / 2f - 100 * guiUnits)
        statsBg.draw(batch)
        coin.setSize(10 * guiUnits, 10 * guiUnits)
        coin.x = statsBg.x + 5 * guiUnits
        coin.setCenterY(statsBg.y + statsBg.height / 2f)
        coin.draw(batch)
        statsBg.setSize(100 * guiUnits, progressbarHeight)
        statsBg.setPosition(-guiCamera.viewportWidth / 2f, statsBg.y - statsBg.height - 10 * guiUnits)
        statsBg.draw(batch)
        statsBg.setSize(100 * guiUnits, progressbarHeight)
        statsBg.setPosition(-guiCamera.viewportWidth / 2f, statsBg.y - statsBg.height)
        statsBg.draw(batch)
    }

    private fun drawStatsText() {
        if (currentPlayer == null) return
        val y = guiCamera.viewportHeight / 2f - 100 * guiUnits + progressbarHeight / 2f + 3 * guiUnits
        statsFont!!.color = Color.WHITE
        //        statsFont.draw(batch, "Coins: " + coinValue, statsBg.getX() + 5 * guiUnits, y);
        statsFont!!.draw(batch, "" + coinValue, statsBg.x + 20 * guiUnits, y)
        statsFont!!.draw(batch, "Blocks: " + currentPlayer!!.numCells, statsBg.x + 5 * guiUnits, y - progressbarHeight - 10 * guiUnits)
        statsFont!!.draw(batch, "Kills: " + currentPlayer!!.kills, statsBg.x + 5 * guiUnits, y - 2 * progressbarHeight - 10 * guiUnits)
    }

    /* ***************************************** LOGIC *******************************************/
    private fun updatePlayersPositions(dt: Float) {
//        synchronized (players) {
//        for (int i = 0; i < players.length; i++) {
//            Player player = players[i];
        synchronized(playersMutex) {
            for (player in players.valueCollection()) {
                if (player == null || player._stroke == null) continue
                if (player.status.toInt() == 0) {

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
                            correctPlayerPosition(player)
                            correctPlayerPositionTime = CORRECT_PLAYER_POSITION_INTERVAL
                        } else correctPlayerPositionTime -= (dt * 1000).toInt()
                    }
                    player._angle += _Math.angleDistance(player._angle, player.new_angle) * PLAYER_ROTATE_SPEED * dt
                    var x = player._stroke!!.x + player._stroke!!.width / 2f
                    var y = player._stroke!!.y + player._stroke!!.height / 2f
                    val newX = x + MathUtils.cos(player.angle) * player.speed * dt
                    val newY = y + MathUtils.sin(player.angle) * player.speed * dt
                    if (newX <= MAP_SIZE_X_EXT_PIXEL && newX >= -MAP_SIZE_X_EXT_PIXEL) x = newX
                    if (newY <= MAP_SIZE_Y_EXT_PIXEL && newY >= -MAP_SIZE_Y_EXT_PIXEL) y = newY
                    player._stroke!!.setCenter(x, y)
                    player._fill!!.setCenter(x, y)
                    if (player.indic != null) {
                        player.indic!!.setCenter(x, y)
                        player.indic!!.rotation = player._angle * MathUtils.radiansToDegrees - 90
                    }
                    player.bcGhost!!.setCenter(player.x, player.y)
                    if (ROTATE_CAMERA) {
                        player._fill!!.setOriginCenter()
                        player._fill!!.rotation = player._angle * MathUtils.radiansToDegrees - 90
                    }

//                    if (ADD_FAKE_PATH_CELLS) {
//                        if (room.state.started && !room.state.ended)
//                            processPlayerPosition(player, x, y);
//                    }
                } else {
                    val x = player.x
                    val y = player.y
                    player._stroke!!.setCenter(x, y)
                    player._fill!!.setCenter(x, y)
                    if (player.indic != null) {
                        player.indic!!.setCenter(x, y)
                        player.indic!!.rotation = player.angle * MathUtils.radiansToDegrees - 90
                    }
                    player.bcGhost!!.setCenter(player.x, player.y)
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
    private fun updateZoom() {
        if (currentPlayer == null) return
        gameCamera.zoom = Math.min(CAMERA_INIT_ZOOM + currentPlayer!!.numCells * 0.0008f, 1.5f)
    }

    private val serverTime: Long
        private get() = System.currentTimeMillis() + timeDiff

    private fun correctPlayerPosition(player: Player) {
        val lerp = calculateLerp(player)

//                    float d = dst > 75 ? dst : dst > 25 ? 5f : Math.min(dst, 1.25f);
        if (lerp > 0) {
            if (player.clientId == room!!.sessionId) {
                println("lerp is $lerp")
            }
            player._stroke!!.setCenterX(MathUtils.lerp(player._stroke!!.x + player._stroke!!.width / 2f, player.x, lerp))
            player._stroke!!.setCenterY(MathUtils.lerp(player._stroke!!.y + player._stroke!!.height / 2f, player.y, lerp))
            player._fill!!.setCenter(player._stroke!!.x + player._stroke!!.width / 2f, player._stroke!!.y + player._stroke!!.height / 2f)
            if (player.indic != null) {
                player.indic!!.setCenter(player._stroke!!.x + player._stroke!!.width / 2f, player._stroke!!.y + player._stroke!!.height / 2f)
            }
            if (lerp > 0.5f) {
                player._angle = player.angle
            }
        }
    }

    private fun calculateLerp(player: Player): Float {
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
        val dst = Vector2.dst2(player._stroke!!.x + player._stroke!!.width / 2f, player._stroke!!.y + player._stroke!!.height / 2f, player.x, player.y)

        // float d = dst > 75 ? dst : dst > 25 ? 5f : Math.min(dst, 1.25f);
        return when {
            dst > 5625 -> { // > 75
                1f
            }
            dst > 625 -> { // > 25
                100f / dst
            }
            dst > 25 -> { // > 5
                10f / dst
            }
            else -> {
                //if (player.clientId.equals(room.getSessionId())) System.out.println("NO LERPING");
//                return 0.3f;
                0f
            }
        }

//        float l = 1 + (dst - 5625) / 6222f;
//        if (l > 1) return 1;
//        if (l < 0.1f) {
//            if (player.clientId.equals(room.getSessionId())) System.out.println("NO LERPING");
//            return 0;
//        }
//        return l;
    }

    private fun getHexPosition(x: Int, y: Int): Vector2 {
        val pos = Vector2()
        pos.x = x * GRID_WIDTH + if (y % 2 == 0) 0f else GRID_WIDTH / 2f
        pos.y = y * GRID_HEIGHT
        return pos
    }

    private fun handleTouchDownDrag(screenX: Int, screenY: Int) {
        if (controllerType == Constants.CONTROL_TOUCH) {
            direction = Math.toDegrees(Math.atan2(screenY.toDouble(), screenX.toDouble())).toFloat()
        } else if (controllerType == Constants.CONTROL_FLOATING && mouseIsDown) {
            thumbstickBgSprite.setCenter(padAnchorPoint.x, padAnchorPoint.y)
            padVector[screenX - padAnchorPoint.x] = screenY - padAnchorPoint.y
            if (padVector.len2() > PAD_CONTROLLER_MAX_LENGTH * PAD_CONTROLLER_MAX_LENGTH) {
                padVector.nor().scl(PAD_CONTROLLER_MAX_LENGTH.toFloat())
            }
            thumbstickPadSprite.setCenter(padAnchorPoint.x + padVector.x, padAnchorPoint.y + padVector.y)
            direction = Math.toDegrees(Math.atan2(padVector.y.toDouble(), padVector.x.toDouble())).toFloat()
        } else if (controllerType == Constants.CONTROL_FIXED_LEFT || controllerType == Constants.CONTROL_FIXED_RIGHT) {
            padVector[screenX - onScreenPadPosition.x] = screenY - onScreenPadPosition.y
            onScreenPadInitLen = padVector.len()
            onScreenPadNorVector = padVector.nor().cpy()
            if (onScreenPadInitLen > PAD_CONTROLLER_MAX_LENGTH) {
                onScreenPadInitLen = PAD_CONTROLLER_MAX_LENGTH.toFloat()
            }
            padVector.scl(onScreenPadInitLen)
            onScreenPadCurrentLen = onScreenPadInitLen
            onScreenPadReleaseTimer = 0f
            thumbstickPadSprite.setCenter(onScreenPadPosition.x + padVector.x, onScreenPadPosition.y + padVector.y)
            direction = Math.toDegrees(Math.atan2(padVector.y.toDouble(), padVector.x.toDouble())).toFloat()
        }
    }

    private fun updateGuiValues() {
        val lessValue = if (guiCamera.viewportWidth < guiCamera.viewportHeight) guiCamera.viewportWidth else guiCamera.viewportHeight
        progressbarWidth = lessValue * 0.6f
        yourProgressbarWidth = lessValue * 0.4f
        progressbarInitWidth = lessValue * 0.25f
        yourProgressbarInitWidth = lessValue * 0.15f
        guiUnits = lessValue * 0.002f
        progressbarGap = -1f //lessValue / 400f;
        progressbarHeight = lessValue / 18f
        progressbarTopMargin = 0f //lessValue / 125f;
        progressbarExtraGapForCurrentPlayer = progressbarHeight + lessValue / 100f
    }

    /* **************************************** NETWORK ******************************************/
    private val roomName: String
        private get() {
            if (gameMode == GAME_MODE_BATTLE) {
                return "battle"
            } else if (gameMode == GAME_MODE_FFA) {
                return "ffa"
            }
            return ""
        }

    private fun connect(): Job {
        return GlobalScope.launch {
            connectToServer()
        }
    }

    private suspend fun connectToServer() {
        println("ConnectToServer...")
        connectionState = CONNECTION_STATE_CONNECTING
        val client = Client(Constants.GAME_ENDPOINT)
        val options = LinkedHashMap<String, Any>()
        options["name"] = prefs.getString(Constants.KEY_PLAYER_NAME, "")
        //String fill = "e" + TextUtil.padLeftZeros((int) (Math.random() * 100) + "", 5);
        val selectedFill = prefs.getInteger(Constants.KEY_SELECTED_IMAGE_INDEX, 0)
        println("PLAYSCREEN: selected image = $selectedFill")
        if (selectedFill >= 0) {
            val fill = "e" + TextUtil.padLeftZeros("" + selectedFill, 5)
            options["fill"] = fill
            println("fill is $fill")
        } else {
            var sc = ColorUtil.FILL_COLORS[prefs.getInteger(Constants.KEY_SELECTED_COLOR, 0)] and 0xFFFFFF
            sc = sc shl 8
            sc = sc or 0xFF
            options["fill"] = "#" + Color(sc)
        }

//        fill = "e00093";
        // System.out.println("fill >>> " + fill);
        var sc = ColorUtil.STROKE_COLORS[prefs.getInteger(Constants.KEY_SELECTED_COLOR, 0)] and 0xFFFFFF
        sc = sc shl 8
        sc = sc or 0xFF
        options["stroke"] = "#" + Color(sc)
        val id = prefs.getString(Constants.KEY_ID, null) // TODO: id on pref?
        if (id != null) options["id"] = id
        try {
            if (sessionId == null) {
                updateRoom(client.joinOrCreate(MyState::class.java, roomName, options))
            } else {
                updateRoom(client.reconnect(MyState::class.java, roomId!!, sessionId!!))
            }
        } catch (e: Exception) {
            if (e.message == "Not Found") {
                // id not found
                println(" - id not found - ")
                prefs.remove(Constants.KEY_ID)
                prefs.flush()
            }
            e.printStackTrace()
            if (connectionState != CONNECTION_STATE_CLOSED) connectionState = CONNECTION_STATE_DISCONNECTED
        }
    }

    private fun updateRoom(room: Room<MyState>) {
        this.room = room
        sessionId = room.sessionId
        roomId = room.id
        println("joined $roomName")
        players.clear()
        for (i in 0 until MAP_SIZE * 2 + 1) {
            for (j in 0 until MAP_SIZE * 2 + 1) {
                cells[i][j] = null
                pathCells[i][j] = null
            }
        }
        connectionState = CONNECTION_STATE_CONNECTED
        isUpdating = true
        registerCallbacks()

        room.onLeave = { code ->
            println("left $roomName code = $code")
            if (code > 1000) {
                if (connectionState != CONNECTION_STATE_CLOSED) connectionState = CONNECTION_STATE_DISCONNECTED
            }
        }

        room.onError = { code, message ->
            if (connectionState != CONNECTION_STATE_CLOSED) connectionState = CONNECTION_STATE_DISCONNECTED
            println("onError($code, $message)")
        }

        room.onStateChange = { state: MyState, isFirstState: Boolean ->
            if (isFirstState) {
                Timer.schedule(object : Timer.Task() {
                    override fun run() {
                        room.send("ready", "let's go")
                        isUpdating = false
                    }
                }, 1f)
            }
        }

        room.onMessage("welcome") { data: LinkedHashMap<String, Any> ->
            val time = data["time"] as Long
            timeDiff = time - System.currentTimeMillis()
            println("time diff: $timeDiff")
            val id = data["id"] as String?
            println("id = $id")
            prefs.putString(Constants.KEY_ID, id).flush()
        }

        room.onMessage("capture") label@{ data: LinkedHashMap<String, Any> ->
            if (connectionState == CONNECTION_STATE_CLOSED) return@label
            val clientId = data["player"] as String?
            val num = data["num"] as Int
            val player = room.state.players[clientId]
            if (player != null) {
                if (player.trailGraphic != null) {
                    Gdx.app.postRunnable { player.trailGraphic!!.truncateAt(0) }
                }
                Timer.schedule(object : Timer.Task() {
                    override fun run() {
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
                        for (i in 0 until 2 * MAP_SIZE + 1) {
                            for (j in 0 until 2 * MAP_SIZE + 1) {
                                val pathCell = pathCells[i][j]
                                if (pathCell != null && pathCell.pid == player.pid) {
                                    pathCells[i][j] = null
                                }
                            }
                        }
                    }
                }, 0.3f)
            }
            if (clientId == room.sessionId) {
                if (num > 4) {
                    if (soundIsOn) Gdx.app.postRunnable { captureSound!!.play() }
                    synchronized(textFadeOutAnimations) {
                        val playerSprite = room.state.players[clientId]?._stroke
                        textFadeOutAnimations.add(TextFadeOutAnimation("+$num blocks", COLOR_BLOCKS_TEXT_FADE_OUT, playerSprite!!.x + playerSprite.width / 2f, playerSprite.y + playerSprite.height / 2f))
                    }
                }
                Gdx.app.log(TAG, "+$num blocks")
            }
        }

        room.onMessage("ping") { data: LinkedHashMap<String, Any> ->
            val t = data["t"] as Long
            val st = data["st"] as Long
            val currentTimeMillis = System.currentTimeMillis()
            timeDiff = st - currentTimeMillis
            // System.out.println("new timeDiff = " + timeDiff);
            if (t == lastPingSentTime) {
                lastPingReplyTime = currentTimeMillis
                currentPing = (lastPingReplyTime - t).toInt()
            } else currentPing = 0
        }

        room.onMessage("death") { data: LinkedHashMap<String, Any> ->
            // dead
            connectionState = CONNECTION_STATE_CLOSED
            val x = java.lang.Double.valueOf(data["x"].toString() + "")
            val y = java.lang.Double.valueOf(data["y"].toString() + "")
            deathPosition[x.toFloat()] = y.toFloat()
            Gdx.app.postRunnable {
                if (soundIsOn) deathSound!!.play()
                gameCamera.zoom = 1f
            }
            println("YOU ARE DEAD!")
            // TODO: show death dialog
        }

        room.onMessage("hit") { data: Any ->
            if (soundIsOn) Gdx.app.postRunnable { hitSound!!.play() }
        }

        room.onMessage("eat") { data: LinkedHashMap<String, Any> ->
            val itemType = data["type"] as Int
            if (itemType == Item.TYPE_COIN) {
                if (soundIsOn) Gdx.app.postRunnable { coinSound!!.play() }
                coinValue = data["coins"] as Int
                prefs.putInteger(Constants.KEY_COINS, coinValue).flush()
                val add = data["add"] as Int
                synchronized(textFadeOutAnimations) {
                    val playerSprite = room.state.players[room.sessionId]?._stroke
                    textFadeOutAnimations.add(TextFadeOutAnimation(
                            "+$add coins",
                            COLOR_COINS_TEXT_FADE_OUT,
                            playerSprite!!.x + playerSprite.width / 2f,
                            playerSprite.y + playerSprite.height / 2f
                    ))
                }
            } else if (itemType == Item.TYPE_BOOST) {
                if (soundIsOn) Gdx.app.postRunnable { boostSound!!.play() }
            }
        }
    }

    private fun registerCallbacks() {
        room!!.state.players.onAdd = label@{ player: Player?, key: String ->
            player!!
            if (connectionState != CONNECTION_STATE_CONNECTED) return@label
            if (player.pid == 0L) return@label
            if (player.clientId == room!!.sessionId) {
                println("OOOOOOOOOOO YOU HAVE BEEN ADDED YOO HAHAHAHA")
            }
            synchronized(arFont) { player._name = arFont.getText(player.name) }
            player._angle = player.angle
            Gdx.app.postRunnable {
                if (player.stroke!!.startsWith("#") && player.stroke!!.length == 9) {
                    player.strokeColor = Color.valueOf(player.stroke)
                    player.progressColor = ColorUtil.getPlayerProgressColor(player.strokeColor)
                    player.pathCellColor = ColorUtil.getPlayerPathCellColor(player.strokeColor)
                } else {
                    player.strokeColor = Color.BLACK
                    player.progressColor = Color.BLACK
                    player.pathCellColor = Color.BLACK
                }
                if (player.clientId == room!!.sessionId) {
                    println("TTTTTTTTTTTTTT player fill is " + player.fill)
                }
                if (player.fill!!.startsWith("#") && player.fill!!.length == 9) {
                    player.fillColor = Color.valueOf(player.fill)
                    player.progressColor = player.fillColor
                } else if (player.progressColor == Color.BLACK) {
                    println(player.stroke + " " + player.fill)
                }
                player.text = GlyphLayout(usernameFont, player._name)
                player._stroke = mainAtlas!!.createSprite(TEXTURE_REGION_BC)
                player._stroke?.setSize(STROKE_SIZE, STROKE_SIZE)
                player._stroke?.setColor(player.strokeColor)
                player._stroke?.setCenter(player.x, player.y)
                if (player.fillColor != null) {
                    player._fill = mainAtlas.createSprite(TEXTURE_REGION_BC)
                    player._fill?.setColor(player.fillColor)
                    player.fillIsTexture = false
                } else {
                    if (graphicsHigh) {
                        player._fill = fillAtlas!!.createSprite(player.fill)
                        if (player.clientId == room!!.sessionId) {
                            println("player fill is " + player.fill)
                        }
                    }
                    if (player._fill == null) {
                        player._fill = mainAtlas.createSprite(TEXTURE_REGION_BC)
                        player._fill!!.setColor(player.progressColor)
                        player.fillIsTexture = false
                    } else {
                        player.fillIsTexture = true
                    }
                }
                player._fill!!.setSize(FILL_SIZE, FILL_SIZE)
                player._fill!!.setCenter(player.x, player.y)
                if (player.clientId == room!!.sessionId) {
                    playerProgressBar.color = player.progressColor
                    player.indic = mainAtlas.createSprite(TEXTURE_REGION_INDIC)
                    player.indic?.setSize(INDIC_SIZE, INDIC_SIZE)
                    player.indic?.setColor(player.strokeColor)
                    player.indic?.setCenter(player.x, player.y)
                    player.indic?.setOriginCenter()
                    player.indic?.setRotation(player.angle * MathUtils.radiansToDegrees - 90)
                }
                player.bcGhost = mainAtlas.createSprite(TEXTURE_REGION_BC)
                player.bcGhost?.setColor(
                        player.strokeColor!!.r,
                        player.strokeColor!!.g,
                        player.strokeColor!!.b,
                        player.strokeColor!!.a / 2f
                )
                player.bcGhost?.setCenter(player.x, player.y)
                player.bcGhost?.setSize(STROKE_SIZE, STROKE_SIZE)
                if (player.clientId == room!!.sessionId) {
                    gameCamera.position.x = player.x
                    gameCamera.position.y = player.y
                }
                player.trailGraphic = TrailGraphic(trailTexture)
                player.trailGraphic?.tint = player.strokeColor
                player.trailGraphic?.ropeWidth = ROPE_WIDTH
                player.trailGraphic?.textureULengthBetweenPoints = 1 / 2f
                player._position = leaderboardList.size + 1
                player._percentage = player.numCells / TOTAL_CELLS.toFloat()
                player.progressBar = mainAtlas.createSprite(TEXTURE_REGION_PROGRESSBAR)
                player.progressBar?.setColor(player.progressColor)
                player.progressBar?.setX(Gdx.graphics.width / 2f - (player._percentage * (progressbarWidth - progressbarInitWidth) + progressbarInitWidth))
                player.progressBar?.setY(Gdx.graphics.height / 2f - progressbarTopMargin - Math.min(player._position - 1, LEADERBOARD_NUM) * (progressbarHeight + progressbarGap) - progressbarHeight)
                players.put(player.pid, player)
                synchronized(leaderboardList) { if (!leaderboardList.contains(player)) leaderboardList.add(player) }
                registerPlayerCallbacks(player)
            }
        }
        room!!.state.players.onRemove = label@{ player: Player?, key: String? ->
            player!!
            if (connectionState != CONNECTION_STATE_CONNECTED) return@label
            if (player.pid == 0L) return@label
            println("player removed, color: " + player.pid)
            if (player.trailGraphic != null) {
                Gdx.app.postRunnable { player.trailGraphic!!.truncateAt(0) }
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
            var i = 0
            while (i < 2 * MAP_SIZE + 1) {
                var j = 0
                while (j < 2 * MAP_SIZE + 1) {
                    var cell = pathCells[i][j]
                    if (cell != null && cell.pid == player.pid) {
                        pathCells[i][j] = null
                    }
                    cell = cells[i][j]
                    if (cell != null && cell.pid == player.pid) {
                        cells[i][j] = null
                    }
                    j++
                }
                i++
            }
            players.remove(player.pid)
            synchronized(leaderboardList) { leaderboardList.remove(player) }
        }
        room!!.state.items.onAdd = { item: Item?, key: String ->
            item!!
            synchronized(items) {
                items[key] = item
            }
            Gdx.app.postRunnable {
                if (connectionState != CONNECTION_STATE_CONNECTED) return@postRunnable
                if (item.type.toInt() == Item.TYPE_COIN) {
                    item.sprite = mainAtlas!!.createSprite(TEXTURE_REGION_COIN)
                } else if (item.type.toInt() == Item.TYPE_BOOST) {
                    item.sprite = mainAtlas!!.createSprite(TEXTURE_REGION_BOOST)
                }
                item.sprite!!.setCenter(item.x, item.y)
            }
        }

        room!!.state.items.onRemove = { item: Item?, key:String ->
            synchronized(items) {
                items.remove(key)
            }
        }
        room!!.state.players.triggerAll()
        room!!.state.items.triggerAll()
    }

    fun registerPlayerCallbacks(player: Player) {
        player.path.onAdd = label@{ point: Point?, key: Int? ->
            point!!
            key!!
            if (connectionState != CONNECTION_STATE_CONNECTED) return@label
            if (player.trailGraphic == null) return@label
            Gdx.app.postRunnable l@{
                if(key == 0) return@l
                val lastPoint = player.path[key - 1]
                if (lastPoint != null) {
                    val dx = point.x - lastPoint.x
                    val dy = point.y - lastPoint.y
                    //                                        player.trailGraphic.setPoint(key2 * 4 - 1, lastPoint.x + 3 * dx / 4f, lastPoint.y + 3 * dy / 4f);
//                                        player.trailGraphic.setPoint(key2 * 4 - 2, lastPoint.x + 2 * dx / 4f, lastPoint.y + 2 * dy / 4f);
//                                        player.trailGraphic.setPoint(key2 * 4 - 3, lastPoint.x + 1 * dx / 4f, lastPoint.y + 1 * dy / 4f);
                    player.trailGraphic!!.setPoint((key - 1) * 2, lastPoint.x, lastPoint.y)
                    player.trailGraphic!!.setPoint(key * 2 - 1, lastPoint.x + dx / 2f, lastPoint.y + dy / 2f)
                    player.trailGraphic!!.setPoint(key * 2, point.x + dx / 2f, point.y + dy / 2f)
                }
            }
        }
        player.path_cells.onAdd = label@{ cell: Cell?, key: Int? ->
            cell!!
            if (connectionState != CONNECTION_STATE_CONNECTED) return@label
            //                            System.out.println("new path cell for player " + player.pid + " name= " + player.name + " cell.pid= " + cell.pid);
            Gdx.app.postRunnable { initPathCellSprite(player, cell) }
            pathCells[cell.x + MAP_SIZE][cell.y + MAP_SIZE] = cell
        }
        player.cells.onAdd = label@{ cell: Cell?, key: String? ->
            cell!!
            if (connectionState != CONNECTION_STATE_CONNECTED) return@label
            Gdx.app.postRunnable { initCellSprite(player, cell) }
            cells[cell.x + MAP_SIZE][cell.y + MAP_SIZE] = cell
        }
        player.cells.onRemove = label@{ cell: Cell?, key: String? ->
            cell!!
            if (connectionState != CONNECTION_STATE_CONNECTED) return@label
            //                            System.out.println("cell remove key : " + key + " --> " + cell.x + ", " + cell.y);
            val c = cells[cell.x + MAP_SIZE][cell.y + MAP_SIZE]
            if (c == null || c.pid != player.pid) return@label
            cells[cell.x + MAP_SIZE][cell.y + MAP_SIZE] = null
        }
        player.cells.triggerAll()
        player.path_cells.triggerAll()
        player.path.triggerAll()
    }

    private fun checkConnection() {
        if (connectionState == CONNECTION_STATE_CONNECTED && lastPingReplyTime > 0 && System.currentTimeMillis() - lastPingReplyTime > 15000) {
            // we are disconnected for sure
            if (connectionState != CONNECTION_STATE_CLOSED) connectionState = CONNECTION_STATE_DISCONNECTED
        }
        if (connectionState == CONNECTION_STATE_DISCONNECTED) {
            connectionJob = connect()
        }
    }

    private fun sendDirection() {
        if (room == null || !room!!.hasJoined()) return
        if (direction != -1000f && lastDirection != direction) {
            room!!.send("angle", direction)
            lastDirection = direction
        }
    }

    private fun sendPing() {
        if (room == null || !room!!.hasJoined()) return
        lastPingSentTime = System.currentTimeMillis()
        room!!.send("ping", lastPingSentTime)
    }

    companion object {
        /* *************************************** CONSTANTS *****************************************/
        private const val DEBUG_SHOW_GHOST = false
        private const val DEBUG_DRAW_PIDS = false
        private const val CORRECT_PLAYER_POSITION = true
        private const val ADD_FAKE_PATH_CELLS = false
        private const val ROTATE_CAMERA = false
        private const val MAP_SIZE = 50
        private const val EXTENDED_CELLS = 4
        private const val TOTAL_CELLS = (2 * MAP_SIZE + 1) * (2 * MAP_SIZE + 1)
        private const val CORRECT_PLAYER_POSITION_INTERVAL = 100
        private const val SEND_DIRECTION_INTERVAL = 200
        private const val CHECK_CONNECTION_INTERVAL = 4000
        private const val SEND_PING_INTERVAL = 5000
        private const val PAD_CONTROLLER_MAX_LENGTH = 42
        private const val LEADERBOARD_NUM = 4
        private const val SCREEN_WIDTH_PORTRAIT = 480
        private const val SCREEN_WIDTH_LANDSCAPE = 800

        //    private static final int PATH_CELLS_UPDATE_TIME = 500;
        private const val GAME_MODE_FFA = 0
        private const val GAME_MODE_BATTLE = 1
        private const val CONNECTION_STATE_DISCONNECTED = 0
        private const val CONNECTION_STATE_CONNECTING = 1
        private const val CONNECTION_STATE_CONNECTED = 2
        private const val CONNECTION_STATE_CLOSED = 3
        private const val CAMERA_LERP = 0.8f
        private const val CAMERA_DEATH_LERP = 0.4f
        private const val CAMERA_INIT_ZOOM = 0.7f
        private const val ON_SCREEN_PAD_RELEASE_TOTAL_TIME = 0.3f
        private const val GRID_WIDTH = 44f
        private const val GRID_HEIGHT = 38f

        //    private static final float MAP_SIZE_X_PIXEL = (MAP_SIZE * GRID_WIDTH);
        private const val MAP_SIZE_X_EXT_PIXEL = (MAP_SIZE + EXTENDED_CELLS) * GRID_WIDTH

        //    private static final float MAP_SIZE_Y_PIXEL = (MAP_SIZE * GRID_HEIGHT);
        private const val MAP_SIZE_Y_EXT_PIXEL = (MAP_SIZE + EXTENDED_CELLS) * GRID_HEIGHT
        private const val LEADERBORAD_CHANGE_SPEED = 90f
        private const val PLAYER_ROTATE_SPEED = 2f

        //    private static final float HIGH_LERP_TIME = 2; // seconds
        private const val ROPE_WIDTH = 20f
        private const val STROKE_SIZE = 46f
        private const val FILL_SIZE = 36f
        private const val INDIC_SIZE = 80f
        private const val CELL_WIDTH = 40f
        private const val CELL_TEX_WIDTH = 34f
        private const val CELL_HEIGHT = 46f

        //    private static final float BC_SIZE = 46 * 1.2f;
        //    private static final float C_SIZE = 36 * 1.2f;
        //    private static final float INDIC_SIZE = 80 * 1.2f;
        private const val TAG = "PlayScreen"
        private const val PATH_FONT_NOTO = "fonts/NotoSans-Regular.ttf"
        private const val PATH_FONT_ARIAL = "fonts/arialbd.ttf"
        private const val PATH_PACK_ATLAS = "gfx/pack.atlas"
        private const val PATH_FILL_ATLAS = "gfx/fill.atlas"
        private const val PATH_TRAIL_TEXTURE = "gfx/trail.png"
        private const val PATH_LOADING_SPRITESHEET = "gfx/loading.png"
        private const val PATH_SOUND_CAPTURE = "sfx/capture1.wav"

        //    private static final String PATH_SOUND_CAPTURE = "sfx/capture2.mp3";
        private const val PATH_SOUND_CLICK = "sfx/click2.wav"
        private const val PATH_SOUND_BOOST = "sfx/boost1.wav"
        private const val PATH_SOUND_COIN = "sfx/coin1.wav"
        private const val PATH_SOUND_HIT = "sfx/hit1.wav"
        private const val PATH_SOUND_DEATH = "sfx/lose1.wav"
        private const val TEXTURE_REGION_HEX_WHITE = "hex_white"
        private const val TEXTURE_REGION_THUMBSTICK_BG = "thumbstick-background"
        private const val TEXTURE_REGION_THUMBSTICK_PAD = "thumbstick-pad"
        private const val TEXTURE_REGION_BC = "bc"
        private const val TEXTURE_REGION_INDIC = "indic"
        private const val TEXTURE_REGION_PROGRESSBAR = "progressbar"
        private const val TEXTURE_REGION_COIN = "coin"
        private const val TEXTURE_REGION_BOOST = "boost"
        private val ON_SCREEN_PAD_RELEASE_ELASTIC_OUT: Interpolation = ElasticOut(3f, 2f, 3, 0.5f)
        private val COLOR_TIME_TEXT_BACKGROUND = Color(0x707070cc)

        //    private static final Color COLOR_KILLS_TEXT_BACKGROUND = new Color(0x707070aa);
        private val COLOR_KILLS_TEXT_BACKGROUND = Color(0x606060aa)
        private val COLOR_YOUR_BEST_PROGRESS_TEXT = Color(0x707070cc)
        private val COLOR_YOUR_PROGRESS_BG = Color(0x70707088)
        private val CONNECTING_TEXT_COLOR = Color.valueOf("#212121ff")
        private val COLOR_BLOCKS_TEXT_FADE_OUT = Color.valueOf("#212121ff")
        private val COLOR_COINS_TEXT_FADE_OUT = Color(-0x7090ce01 /*0xa67c00ff*/)

        //    private static final Color COLOR_COINS_TEXT_FADE_OUT = Color.valueOf("#212121ff");
        private val SORT_PLAYERS_BY_NUM_CELLS = Comparator { o1: Player, o2: Player -> Integer.compare(o2.numCells, o1.numCells) }
        private val SORT_PLAYERS_BY_POSITION = Comparator { o1: Player, o2: Player -> Integer.compare(o1._position, o2._position) }
        private val playersMutex = Any()
    }

    /* ************************************** CONSTRUCTOR ****************************************/
    init {
        prefs = Gdx.app.getPreferences(Constants.PREFS_NAME)
        langCode = I18N.getLangCode(prefs.getString(Constants.KEY_SETTINGS_LANGUAGE, Constants.DEFAULT_SETTINGS_LANGUAGE))
        soundIsOn = prefs.getBoolean(Constants.KEY_SETTINGS_SOUND, true)
        graphicsHigh = prefs.getString(Constants.KEY_SETTINGS_GRAPHICS, Constants.DEFAULT_SETTINGS_GRAPHICS) == "high"
        controllerType = prefs.getInteger(Constants.KEY_SETTINGS_CONTROL, Constants.DEFAULT_SETTINGS_CONTROL)
        coinValue = prefs.getInteger(Constants.KEY_COINS, 0)
        val isRotationVectorAvailable = Gdx.input.isPeripheralAvailable(Input.Peripheral.RotationVector)
        val isAccelerometerAvailable = Gdx.input.isPeripheralAvailable(Input.Peripheral.Accelerometer)
        val isCompassAvailable = Gdx.input.isPeripheralAvailable(Input.Peripheral.Compass)
        deviceRotationAvailable = isRotationVectorAvailable || isAccelerometerAvailable && isCompassAvailable
        if (controllerType == Constants.CONTROL_DEVICE_ROTATION && !deviceRotationAvailable) {
            controllerType = Constants.CONTROL_TOUCH
        }
        batch = SpriteBatch()
        mainAtlas = TextureAtlas(PATH_PACK_ATLAS)
        if (graphicsHigh) fillAtlas = TextureAtlas(PATH_FILL_ATLAS)
        whiteHex = mainAtlas.findRegion(TEXTURE_REGION_HEX_WHITE)
        thumbstickBgSprite = mainAtlas.createSprite(TEXTURE_REGION_THUMBSTICK_BG)
        thumbstickBgSprite.setSize(152f, 152f)
        thumbstickPadSprite = mainAtlas.createSprite(TEXTURE_REGION_THUMBSTICK_PAD)
        thumbstickPadSprite.setSize(70f, 70f)
        timeBg = mainAtlas.createSprite(TEXTURE_REGION_PROGRESSBAR)
        statsBg = mainAtlas.createSprite(TEXTURE_REGION_PROGRESSBAR)
        youWillRspwnBg = mainAtlas.createSprite(TEXTURE_REGION_PROGRESSBAR)
        youWillRspwnBg.color = COLOR_TIME_TEXT_BACKGROUND
        timeBg.color = COLOR_TIME_TEXT_BACKGROUND
        statsBg.color = COLOR_KILLS_TEXT_BACKGROUND
        playerProgressBar = mainAtlas.createSprite(TEXTURE_REGION_PROGRESSBAR)
        playerProgressBarBest = mainAtlas.createSprite(TEXTURE_REGION_PROGRESSBAR)
        playerProgressBarBest.color = COLOR_YOUR_PROGRESS_BG
        loadingAnimation = LoadingAnimation(PATH_LOADING_SPRITESHEET)
        coin = mainAtlas.createSprite(TEXTURE_REGION_COIN)
        gameCamera = OrthographicCamera()
        gameCamera.zoom = CAMERA_INIT_ZOOM
        fixedCamera = OrthographicCamera()
        guiCamera = OrthographicCamera()
        if (soundIsOn) {
            boostSound = Gdx.audio.newSound(Gdx.files.internal(PATH_SOUND_BOOST))
            coinSound = Gdx.audio.newSound(Gdx.files.internal(PATH_SOUND_COIN))
            clickSound = Gdx.audio.newSound(Gdx.files.internal(PATH_SOUND_CLICK))
            deathSound = Gdx.audio.newSound(Gdx.files.internal(PATH_SOUND_DEATH))
            hitSound = Gdx.audio.newSound(Gdx.files.internal(PATH_SOUND_HIT))
            captureSound = Gdx.audio.newSound(Gdx.files.internal(PATH_SOUND_CAPTURE))
        }
        init(Gdx.graphics.width, Gdx.graphics.height)
        gameCamera.viewportWidth = screenWidth.toFloat()
        gameCamera.viewportHeight = screenHeight.toFloat()
        gameCamera.update()
        fixedCamera.viewportWidth = screenWidth.toFloat()
        fixedCamera.viewportHeight = screenHeight.toFloat()
        fixedCamera.update()
        guiCamera.viewportWidth = Gdx.graphics.width.toFloat()
        guiCamera.viewportHeight = Gdx.graphics.height.toFloat()
        guiCamera.update()
        updateGuiValues()
        trailTexture = Texture(Gdx.files.internal(PATH_TRAIL_TEXTURE), true)
        trailTexture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear)
        trailTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat)
        initFonts()
        timeText = GlyphLayout(timeFont, "99:99")
        youWillRspwnText = GlyphLayout(timeFont, I18N.texts[langCode][I18N.you_will_respawn_in_9_seconds]) // TODO: handle rtl :/
        yourProgressText = GlyphLayout(leaderboardFont, "99.99%")
        yourProgressBestText = GlyphLayout(leaderboardFont, "BEST 99.99%")
        initTiles()
        initInput()
        initControllers()
    }
}