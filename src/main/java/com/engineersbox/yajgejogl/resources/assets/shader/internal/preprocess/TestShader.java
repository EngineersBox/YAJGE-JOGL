package com.engineersbox.yajgejogl.resources.assets.shader.internal.preprocess;

import com.engineersbox.yajgejogl.rendering.RenderContext;
import com.engineersbox.yajgejogl.rendering.shader.ShaderRenderHandler;
import com.engineersbox.yajgejogl.resources.assets.shader.Shader;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;

import java.nio.IntBuffer;

//@RenderHandler(
//        name = TestShader.SHADER_NAME,
//        priority = 0,
//        stage = ShaderStage.PRE_PROCESS
//)
public class TestShader extends ShaderRenderHandler {

    private static final float[] vertices = new float[]{
            // Positions            // Colours
            0.5f,  -0.5f, 0.0f,     1.0f, 0.0f, 0.0f,  // bottom right
            -0.5f, -0.5f, 0.0f,     0.0f, 1.0f, 0.0f,  // bottom left
            0.0f,   0.5f, 0.0f,     0.0f, 0.0f, 1.0f   // top
    };

    private final IntBuffer VAO;
    private final IntBuffer VBO;

    public static final String SHADER_NAME = "test shader";

    public TestShader(final GL2 gl) {
        super(
                gl,
                new Shader(gl)
                        .withVertex("shaders/test/test.vert")
                        .withFragment("shaders/test/test.frag")
        );
        super.shader.build();
        this.VAO = Buffers.newDirectIntBuffer(1);
        this.VBO = Buffers.newDirectIntBuffer(1);
        gl.glGenVertexArrays(1, this.VAO);
        gl.glGenBuffers(1, this.VBO);
        gl.glBindVertexArray(this.VAO.get(0));

        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, this.VBO.get(0));
        gl.glBufferData(GL2.GL_ARRAY_BUFFER, (long) vertices.length * Buffers.SIZEOF_FLOAT, Buffers.newDirectFloatBuffer(vertices), GL2.GL_STATIC_DRAW);

        // position attribute
        gl.glEnableVertexAttribArray(0);
        gl.glVertexAttribPointer(0, 3, GL2.GL_FLOAT, false, 6 * Buffers.SIZEOF_FLOAT, 0);
        // color attribute
        gl.glEnableVertexAttribArray(1);
        gl.glVertexAttribPointer(1, 3, GL2.GL_FLOAT, false, 6 * Buffers.SIZEOF_FLOAT, 3 * Buffers.SIZEOF_FLOAT);
    }

    @Override
    public void render(final RenderContext context) {
        super.shader.bind();
        this.gl.glBindVertexArray(this.VAO.get(0));
        this.gl.glDrawArrays(GL2.GL_TRIANGLES, 0, 3);
        super.shader.unbind();
    }
}
