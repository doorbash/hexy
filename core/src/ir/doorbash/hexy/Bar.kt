package ir.doorbash.hexy

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.Matrix4

/**
 * Created by Milad Doorbash on 7/28/2019.
 */
class Bar {
    private var mesh: Mesh? = null
    private var leftX = 0f
    private var bottomY = 0f
    private var width = 0f
    private var height = 0f
    private val verts = FloatArray(30)
    private var tint = Color.RED
    fun setTint(color: Color) {
        tint = color
    }

    fun setValues(leftX: Float, bottomY: Float, width: Float, height: Float) {
        this.leftX = leftX
        this.bottomY = bottomY
        this.width = width
        this.height = height
        updateVertices()
    }

    private fun updateVertices() {
        var i = 0

        // left bottom
        verts[i++] = leftX //x
        verts[i++] = bottomY //y
        verts[i++] = 0.0f //u
        verts[i++] = 0.0f //v

        // left top
        verts[i++] = leftX
        verts[i++] = bottomY + height
        verts[i++] = 0.0f // u
        verts[i++] = 1.0f // v

        // right bottom
        verts[i++] = leftX + width // x
        verts[i++] = bottomY // y
        verts[i++] = 1.0f // u
        verts[i++] = 0f // v

        // right top
        verts[i++] = leftX + width // x
        verts[i++] = bottomY + height // y
        verts[i++] = 1.0f // u
        verts[i++] = 1.0f // v

        // Create a mesh out of two triangles rendered clockwise without indices
        mesh = Mesh(true, 16, 0,
                VertexAttribute(VertexAttributes.Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE),
                VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"))
        mesh!!.setVertices(verts, 0, 16)
    }

    fun render(proj: Matrix4?) {
//        Gdx.gl.glDepthMask(false);
        Gdx.gl20.glEnable(GL20.GL_BLEND)
        Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        //        texture.bind();
        shader!!.begin()
        shader!!.setUniformMatrix("u_projTrans", proj)
        // shader.setUniformi("u_texture", 0);
        shader!!.setUniformf("a_tint", tint.r, tint.g, tint.b, 1.0f)
        mesh!!.render(shader, GL20.GL_TRIANGLE_STRIP)
        shader!!.end()
        //        Gdx.gl.glDepthMask(true);
    }

    companion object {
        private var shader: ShaderProgram? = null
        fun init() {
            shader = ShaderProgram(
                    Gdx.files.internal("shaders/bar_vertex.glsl").readString(),
                    Gdx.files.internal("shaders/bar_fragment.glsl").readString()
            )
            if (!shader!!.isCompiled) {
                System.err.println(shader!!.log)
            }
        }
    }
}