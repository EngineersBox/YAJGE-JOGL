package com.engineersbox.yajgejogl.resources.assets.material;

import com.engineersbox.yajgejogl.resources.loader.TextureLoader;
import com.jogamp.opengl.GL2;

public class Texture {

    private final com.jogamp.opengl.util.texture.Texture texture;
    private int rows = 1;
    private int cols = 1;

    public Texture(final GL2 gl, final String fileName) {
        this.texture = TextureLoader.load(gl, fileName);
        this.texture.setTexParameteri(gl, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR_MIPMAP_NEAREST);
        this.texture.setTexParameteri(gl, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
        this.texture.setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT);
        this.texture.setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_T, GL2.GL_REPEAT);
        gl.glGenerateMipmap(GL2.GL_TEXTURE_2D);
    }

    public Texture(final com.jogamp.opengl.util.texture.Texture texture) {
        this.texture = texture;
    }

    public Texture(final GL2 gl,
                   final String fileName,
                   final int cols,
                   final int rows)  {
        this(gl, fileName);
        this.cols = cols;
        this.rows = rows;
    }

    public int getCols() {
        return this.cols;
    }

    public int getRows() {
        return this.rows;
    }

    public int getWidth() {
        return this.texture.getWidth();
    }

    public int getHeight() {
        return this.texture.getHeight();
    }

    public void bind(final GL2 gl) {
        this.texture.bind(gl);
    }

    public int getId(final GL2 gl) {
        return this.texture.getTextureObject(gl);
    }

    public void destroy(final GL2 gl) {
        this.texture.destroy(gl);
    }
}
