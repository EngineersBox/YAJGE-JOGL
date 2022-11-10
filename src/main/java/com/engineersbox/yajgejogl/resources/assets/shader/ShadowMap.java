package com.engineersbox.yajgejogl.resources.assets.shader;

import com.engineersbox.yajgejogl.resources.config.io.ConfigHandler;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;

import java.nio.IntBuffer;

public class ShadowMap {

    public static final int SHADOW_MAP_WIDTH = 1024;
    public static final int SHADOW_MAP_HEIGHT = 1024;

    private final IntBuffer depthMapFBO;
    private final IntBuffer depthMap;
    private final GL2 gl;

    public ShadowMap(final GL2 gl) {
        this.gl = gl;
        this.depthMapFBO = Buffers.newDirectIntBuffer(1);
        gl.glGenFramebuffers(1, this.depthMapFBO);
        this.depthMap = Buffers.newDirectIntBuffer(1);
        gl.glGenTextures(1, this.depthMap);
        gl.glBindTexture(GL2.GL_TEXTURE_2D, this.depthMap.get(0));
        gl.glTexImage2D(
                GL2.GL_TEXTURE_2D,
                0,
                GL2.GL_DEPTH_COMPONENT,
                ConfigHandler.CONFIG.render.lighting.shadowMapWidth,
                ConfigHandler.CONFIG.render.lighting.shadowMapHeight,
                0,
                GL2.GL_DEPTH_COMPONENT,
                GL2.GL_FLOAT,
                null
        );
        gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
        gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);
        gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);

        gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, this.depthMapFBO.get(0));
        gl.glFramebufferTexture2D(
                GL2.GL_FRAMEBUFFER,
                GL2.GL_DEPTH_ATTACHMENT,
                GL2.GL_TEXTURE_2D,
                this.depthMap.get(0),
                0
        );
        gl.glDrawBuffer(GL2.GL_NONE);
        gl.glReadBuffer(GL2.GL_NONE);

        if (gl.glCheckFramebufferStatus(GL2.GL_FRAMEBUFFER) != GL2.GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Could not create FrameBuffer");
        }

        gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
    }

    public int getDepthMapTexture() {
        return this.depthMap.get(0);
    }

    public int getDepthMapFBO() {
        return this.depthMapFBO.get(0);
    }

    public void cleanup() {
        this.gl.glDeleteFramebuffers(1, this.depthMapFBO);
    }
}
