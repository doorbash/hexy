package ir.doorbash.hexy;

/**
 * Created by Milad Doorbash on 7/27/2019.
 */

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TrailGraphic {
    private static final String VERTEX_PATH = "shaders/trail_vertex.glsl";
    private static final String FRAGMENT_PATH = "shaders/trail_fragment.glsl";

    private static ShaderProgram shader = null;

    private float alpha = 1.0f;
    private Mesh mesh = null;
    private List<Vector2> points = new ArrayList();
    private float ropeWidth = 1.0f;
//    private Texture texture;
    private float textureULengthBetweenPoints = 1.0f;
    private Color tint = Color.WHITE;
    private float[] vertices = new float[256];

    public static void init() {
        shader = new ShaderProgram(
                Gdx.files.internal(VERTEX_PATH).readString(),
                Gdx.files.internal(FRAGMENT_PATH).readString()
        );
        if (!shader.isCompiled()) {
            System.err.println(shader.getLog());
        }
    }

//    public TrailGraphic(Texture t) {
//        texture = t;
//    }

    public Color getTint() {
        return tint;
    }

    public void setTint(Color color) {
        tint = color;
    }

    public void resizePointCount(int i) {
        while (points.size() < i) {
            points.add(null);
        }
        while (points.size() > i) {
            points.remove(points.size() - 1);
        }
        while (vertices.length < i * 8) {
            vertices = Arrays.copyOf(vertices, vertices.length * 2);
        }
    }

    private void computeVertices(int index, Vector2 point) {

        Vector2 pointAtIndex = points.get(index);
        float currentX;
        float currentY;
        if (index == 0 || points.get(index - 1) == null) {
            if (pointAtIndex != null) {
                currentX = pointAtIndex.x;
                currentY = pointAtIndex.y;
            } else {
                currentX = point.x;
                currentY = point.y;
            }
        } else {
            currentX = points.get(index - 1).x;
            currentY = points.get(index - 1).y;
        }
        float nextX;
        float nextY;
        if (index != points.size() - 1) {
            Vector2 p;
            if ((p = points.get(index + 1)) != null) {
                nextX = p.x;
                nextY = p.y;
            } else {
                nextX = point.x;
                nextY = point.y;
            }
        } else {
            nextX = pointAtIndex.x;
            nextY = pointAtIndex.y;
        }

        float dy = nextY - currentY;
        float dx = -(nextX - currentX);
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        float cos = dy / distance;
        float sin = dx / distance;

        if (pointAtIndex != null) point = pointAtIndex;

        vertices[index * 8 + 0] = point.x + ropeWidth * cos / 2.0f; // x
        vertices[index * 8 + 1] = point.y + ropeWidth * sin / 2.0f; // y
        vertices[index * 8 + 2] = textureULengthBetweenPoints * index; // u
        vertices[index * 8 + 3] = 0; // v

        vertices[index * 8 + 4] = point.x - cos * ropeWidth / 2.0f; // x
        vertices[index * 8 + 5] = point.y - sin * ropeWidth / 2.0f; // y
        vertices[index * 8 + 6] = textureULengthBetweenPoints * index; // u
        vertices[index * 8 + 7] = 1.0f; // v

        if (mesh != null) mesh.setVertices(vertices, 0, points.size() * 8);
    }

    public void setPoint(int index, float x, float y) {
        if (index >= points.size()) {
            resizePointCount(index + 1);
        }
        Vector2 point = new Vector2(x, y);
        points.set(index, point);
        for (int i = index - 1; i > 0; i--) {
            if (points.get(i - 1) != null) {
                break;
            }
            computeVertices(i - 1, point);
        }
        if (index > 0) computeVertices(index - 1, point);

        computeVertices(index, point);
        if (index < points.size() - 1) {
            computeVertices(index + 1, point);
        }
    }

    public void render(Matrix4 matrix4) {
        if (mesh == null) {
            mesh = new Mesh(false, Short.MAX_VALUE, 0,
                    new VertexAttribute(VertexAttributes.Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE),
                    new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0")
            );
            mesh.setVertices(vertices, 0, points.size() * 8);
        }
//        Matrix4 matrix4 = new Matrix4();
//        matrix4.set(batch.getProjectionMatrix());
//        matrix4.mul(batch.getTransformMatrix());
        Gdx.gl.glDepthMask(false);
        Gdx.gl20.glEnable(GL20.GL_BLEND);
        Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
//        texture.bind();
        shader.begin();
        shader.setUniformMatrix("u_projTrans", matrix4);
//        shader.setUniformi("u_texture", 0);
        shader.setUniformf("a_tint", tint.r, tint.g, tint.b, alpha);
        mesh.render(shader, GL20.GL_TRIANGLE_STRIP);
        shader.end();
        Gdx.gl.glDepthMask(true);
    }

    public float getRopeWidth() {
        return ropeWidth;
    }

    public void setRopeWidth(float f) {
        ropeWidth = f;
        Vector2 point = null;
        for (int i = points.size() - 1; i >= 0; i--) {
            if (points.get(i) != null) point = points.get(i);
            computeVertices(i, point);
        }
    }

    public float getTextureULengthBetweenPoints() {
        return textureULengthBetweenPoints;
    }

    public void setTextureULengthBetweenPoints(float f) {
        textureULengthBetweenPoints = f;
    }

    public float[] getVertices() {
        return vertices;
    }

    public void truncateAt(int i) {
        resizePointCount(i);
        if (mesh != null) mesh.setVertices(vertices, 0, points.size() * 8);
    }

    public List<Vector2> getPoints() {
        return points;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float f) {
        alpha = f;
    }
}
