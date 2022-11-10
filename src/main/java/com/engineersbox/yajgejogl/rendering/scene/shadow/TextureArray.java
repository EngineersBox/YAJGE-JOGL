package com.engineersbox.yajgejogl.rendering.scene.shadow;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class TextureArray {

    private final IntBuffer ids;
    private final int width;
    private final int height;
    private final GL2 gl;

    public TextureArray(final GL2 gl,
                        final int numTextures,
                        final int width,
                        final int height,
                        final int pixelFormat) {
        this.gl = gl;
        this.ids = Buffers.newDirectIntBuffer(numTextures);
        gl.glGenTextures(numTextures, this.ids);
        this.width = width;
        this.height = height;

        for (int i = 0; i < numTextures; i++) {
            gl.glBindTexture(GL2.GL_TEXTURE_2D, this.ids.get(i));
            gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_DEPTH_COMPONENT, this.width, this.height, 0, pixelFormat, GL2.GL_FLOAT, (ByteBuffer) null);
            gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
            gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
            gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_COMPARE_MODE, GL2.GL_NONE);
            gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
            gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);
        }
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public IntBuffer getIds() {
        return this.ids;
    }

    public void cleanup() {
        gl.glDeleteTextures(this.ids.capacity(), this.ids);
    }
}
