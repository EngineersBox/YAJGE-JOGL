package com.engineersbox.yajgejogl.rendering.scene.shadow;

import com.engineersbox.yajgejogl.resources.assets.shader.internal.preprocess.DepthShader;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;

import java.nio.IntBuffer;

public class ShadowBuffer {

    public static final int SHADOW_MAP_WIDTH = (int) Math.pow(65, 2);
    public static final int SHADOW_MAP_HEIGHT = SHADOW_MAP_WIDTH;

    private final IntBuffer depthMapFBO;
    private final TextureArray depthMap;
    private final GL2 gl;

    public ShadowBuffer(final GL2 gl) {
        this.gl = gl;
        this.depthMapFBO = Buffers.newDirectIntBuffer(1);
        gl.glGenFramebuffers(1, this.depthMapFBO);
        this.depthMap = new TextureArray(
                gl,
                DepthShader.NUM_CASCADES,
                SHADOW_MAP_WIDTH,
                SHADOW_MAP_HEIGHT,
                GL2.GL_DEPTH_COMPONENT
        );

        gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, this.depthMapFBO.get(0));
        gl.glFramebufferTexture2D(
                GL2.GL_FRAMEBUFFER,
                GL2.GL_DEPTH_ATTACHMENT,
                GL2.GL_TEXTURE_2D,
                this.depthMap.getIds().get(0),
                0
        );
        gl.glDrawBuffer(GL2.GL_NONE);
        gl.glReadBuffer(GL2.GL_NONE);

        if (gl.glCheckFramebufferStatus(GL2.GL_FRAMEBUFFER) != GL2.GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Framebuffer could not be created, left in incomplete state");
        }

        gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
    }

    public TextureArray getDepthMapTexture() {
        return this.depthMap;
    }

    public int getDepthMapFBO() {
        return this.depthMapFBO.get(0);
    }

    public void bindTextures(final int start) {
        for (int i = 0; i < DepthShader.NUM_CASCADES; i++) {
            this.gl.glActiveTexture(start + i);
            this.gl.glBindTexture(GL2.GL_TEXTURE_2D, this.depthMap.getIds().get(i));
        }
    }
    
    public void cleanup() {
        gl.glDeleteFramebuffers(1, this.depthMapFBO);
        this.depthMap.cleanup();
    }
    
}
