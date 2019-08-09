package ir.doorbash.hexy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;

/**
 * Created by Milad Doorbash on 7/28/2019.
 */
public class Bar {

    private static ShaderProgram shader;

    private Mesh mesh;
    private float leftX;
    private float bottomY;
    private float width;
    private float height;
    private float[] verts = new float[30];
    private Color tint = Color.RED;

    static void init() {
        shader = new ShaderProgram(
                Gdx.files.internal("shaders/bar_vertex.glsl").readString(),
                Gdx.files.internal("shaders/bar_fragment.glsl").readString()
        );
        if (!shader.isCompiled()) {
            System.err.println(shader.getLog());
        }
    }

    public void setTint(Color color) {
        tint = color;
    }

    void setValues(float leftX, float bottomY, float width, float height) {
        this.leftX = leftX;
        this.bottomY = bottomY;
        this.width = width;
        this.height = height;
        updateVertices();
    }

    private void updateVertices() {
        int i = 0;

        // left bottom
        verts[i++] = leftX;   //x
        verts[i++] = bottomY ; //y
        verts[i++] = 0.0f;   //u
        verts[i++] = 0.0f;   //v

        // left top
        verts[i++] = leftX;
        verts[i++] = bottomY + height;
        verts[i++] = 0.0f; // u
        verts[i++] = 1.0f; // v

        // right bottom
        verts[i++] = leftX + width; // x
        verts[i++] = bottomY; // y
        verts[i++] = 1.0f; // u
        verts[i++] = 0f; // v

        // right top
        verts[i++] = leftX + width; // x
        verts[i++] = bottomY + height; // y
        verts[i++] = 1.0f; // u
        verts[i++] = 1.0f; // v

        // Create a mesh out of two triangles rendered clockwise without indices
        mesh = new Mesh(true, 16, 0,
                new VertexAttribute(VertexAttributes.Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"));

        mesh.setVertices(verts, 0, 16);
    }

    void render(Matrix4 proj) {
//        Gdx.gl.glDepthMask(false);
        Gdx.gl20.glEnable(GL20.GL_BLEND);
        Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
//        texture.bind();
        shader.begin();
        shader.setUniformMatrix("u_projTrans", proj);
        // shader.setUniformi("u_texture", 0);
        shader.setUniformf("a_tint", tint.r, tint.g, tint.b, 1.0f);
        mesh.render(shader, GL20.GL_TRIANGLE_STRIP);
        shader.end();
//        Gdx.gl.glDepthMask(true);
    }

}
