package ir.doorbash.hexy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import io.colyseus.Client;
import io.colyseus.Room;
import io.colyseus.serializer.schema.DataChange;
import io.colyseus.serializer.schema.Schema;
import ir.doorbash.hexy.model.MyState;
import ir.doorbash.hexy.model.Player;
import ir.doorbash.hexy.util.Color4;
import ir.doorbash.hexy.util.ColorUtil;

/**
 * Created by Milad Doorbash on 7/22/2019.
 */
public class PlayScreen extends ScreenAdapter {

    public static final boolean DEBUG_SHOW_GHOST = false;

    public static final boolean CORRECT_PLAYER_POSITION = true;
    public static final int CORRECT_PLAYER_POSITION_INTERVAL = 100;

    public static final int SEND_DIRECTION_INTERVAL = 500;

    public static final float CAMERA_LERP = 0.9f;

    SpriteBatch batch;
    OrthographicCamera camera;
    Viewport viewport;

    TextureAtlas hexAtlas;
    TextureAtlas gameAtlas;

    Sprite white_hex;
    Texture whiteHex;
    FrameBuffer fbo;
    Sprite tiles;

    //    private Vector2 position = new Vector2();
    private Client client;
    private Room<MyState> room;
    private long timeDiff;
    float lastAngle;
    private LinkedHashMap<String, Object> message;
    int correctPlayerPositionTime = CORRECT_PLAYER_POSITION_INTERVAL;
    int sendDirectionTime = SEND_DIRECTION_INTERVAL;

    public PlayScreen() {
        batch = new SpriteBatch();
        whiteHex = new Texture("spritesheets/hex_white.png");
        white_hex = new Sprite(whiteHex);
        hexAtlas = new TextureAtlas("spritesheets/hex3.txt");
        gameAtlas = new TextureAtlas("spritesheets/game.atlas");
//        white_hex = hexAtlas.createSprite("hex_blue");
//        blue_hex.setSize(40,46);
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(480, 800, camera);
        camera.zoom = 1f;

        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, 960, 1600, false);
        fbo.begin();

        Gdx.gl.glClearColor(0.92f, 0.92f, 0.92f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Matrix4 m = new Matrix4();
        m.setToOrtho2D(0, 0, fbo.getWidth(), fbo.getHeight());

        batch.setProjectionMatrix(m);

        batch.begin();

        for (int xi = -3; xi < 22; xi++) {
            for (int yi = -3; yi < 37; yi++) {
                batch.draw(white_hex, xi * 44 + (yi % 2 == 0 ? 0 : 22), yi * 38, 40, 46);
            }
        }

        batch.end();

        fbo.end();

        tiles = new Sprite(fbo.getColorBufferTexture());

        tiles.flip(false, true);

        message = new LinkedHashMap<>();

        connectToServer();
    }

    @Override
    public void render(float dt) {
        Gdx.gl.glClearColor(0.92f, 0.92f, 0.92f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (room != null) {
            Player player = room.getState().players.get(client.getId());
            if (player != null) {
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
            drawPlayers();
        }

        batch.end();

        if (sendDirectionTime < 0) {
            sendDirection();
            sendDirectionTime = SEND_DIRECTION_INTERVAL;
        } else sendDirectionTime -= dt * 1000;
    }

    @Override
    public void dispose() {
        batch.dispose();
        fbo.dispose();
        hexAtlas.dispose();
    }

    public void resize(int width, int height) {
        viewport.update(width, height);
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
        Schema.MapSchema<Player> players = room.getState().players;
        Iterator<String> iterator = new ArrayList<>(players.keys()).iterator();
        while (iterator.hasNext()) {
            Player player = players.get(iterator.next());
            if (player != null) {
                player.bc.draw(batch);
                if (DEBUG_SHOW_GHOST) player.bcGhost.draw(batch);
            }
        }
    }

    private void updatePlayersPositions(float dt) {
        for (Player player : room.getState().players.values()) {
            if (player.bc != null) {

                if (CORRECT_PLAYER_POSITION) {
                    if (correctPlayerPositionTime < 0) {
                        correctPlayerPosition(player);
                        correctPlayerPositionTime = CORRECT_PLAYER_POSITION_INTERVAL;
                    } else correctPlayerPositionTime -= dt * 1000;
                }

                player.bc.translate(MathUtils.cos(player.angle) * player.speed * dt, MathUtils.sin(player.angle) * player.speed * dt);

                player.bcGhost.setCenter(player.x, player.y);
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
        }
    }

    private void connectToServer() {
        client = new Client("ws://192.168.1.134:3333", new Client.Listener() {
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
                            player.bc = gameAtlas.createSprite("bc");
                            player.bcGhost = gameAtlas.createSprite("bc");
                            Color4 c = ColorUtil.color_index_to_rgba[player.color - 1];
                            player.bc.setColor(c.r, c.g, c.b, c.a);
                            player.bc.setCenter(player.x, player.y);
                            player.bcGhost.setColor(c.r, c.g, c.b, c.a / 2f);
                            if (player.clientId.equals(client.getId())) {
                                // this is you
                                camera.position.x = player.x;
                                camera.position.y = player.y;
                            }
                            player.onChange = changes -> {
                                for (DataChange change : changes) {
                                    switch (change.field) {
                                        case "x":
                                        case "y":

                                            break;
                                    }
                                }
                            };
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
        if (room == null) return;
        float dx = Gdx.input.getX() - Gdx.graphics.getWidth() / 2f;
        float dy = Gdx.input.getY() - Gdx.graphics.getHeight() / 2f;
        int angle = (int) Math.toDegrees(Math.atan2(-dy, dx));
        if (lastAngle != angle) {
            message.put("a", angle);
            room.send(message);
            lastAngle = angle;
        }
    }
}
