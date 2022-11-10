package com.engineersbox.yajgejogl.core;

import com.engineersbox.yajgejogl.resources.config.io.ConfigHandler;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.awt.GLJPanel;
import org.joml.Matrix4f;

public class Window extends GLJPanel {

    private final Matrix4f projectionMatrix;

    public Window(final GLCapabilities caps) {
        super(caps);
        this.projectionMatrix = new Matrix4f();
    }

    public Matrix4f getProjectionMatrix() {
        return this.projectionMatrix;
    }

    public Matrix4f updateProjectionMatrix() {
        final float aspectRatio = (float) this.getWidth() / (float) this.getHeight();
        return this.projectionMatrix.setPerspective(
                (float) Math.toRadians(ConfigHandler.CONFIG.render.camera.fov),
                aspectRatio,
                (float) ConfigHandler.CONFIG.render.camera.zNear,
                (float) ConfigHandler.CONFIG.render.camera.zFar
        );
    }
}
