package com.engineersbox.yajgejogl;

import com.engineersbox.yajgejogl.core.Window;
import com.engineersbox.yajgejogl.resources.assets.gui.font.FontTexture;
import com.engineersbox.yajgejogl.resources.assets.material.Material;
import com.engineersbox.yajgejogl.resources.loader.OBJLoader;
import com.engineersbox.yajgejogl.scene.element.SceneElement;
import com.engineersbox.yajgejogl.scene.element.object.composite.Mesh;
import com.engineersbox.yajgejogl.scene.gui.IHud;
import com.engineersbox.yajgejogl.scene.gui.TextElement;
import com.jogamp.opengl.GL2;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;

import java.awt.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Hud implements IHud {
    private static final Font FONT = new Font("Arial", Font.PLAIN, 20);
    private static final Charset CHARSET = StandardCharsets.ISO_8859_1;

    private final SceneElement[] sceneElements;
    private final TextElement statusTextElement;
    private final SceneElement compassItem;

    public Hud(final GL2 gl,
               final String statusText) {
        final FontTexture fontTexture = new FontTexture(FONT, CHARSET);
        this.statusTextElement = new TextElement(gl, statusText, fontTexture);
        this.statusTextElement.getMesh()
                .getMaterial()
                .setAmbientColour(new Vector4f(1, 1, 1, 1));
        final Mesh mesh = OBJLoader.load(gl, "models/compass.obj");
        final Material material = new Material();
        material.setAmbientColour(new Vector4f(1, 0, 0, 1));
        mesh.setMaterial(material);
        this.compassItem = new SceneElement(mesh);
        this.compassItem.setScale(40.0f);

        this.compassItem.setRotation(new Quaternionf(new AxisAngle4f(180f, 0f, 0f ,0f)));
        this.sceneElements = new SceneElement[]{ this.statusTextElement, this.compassItem };
    }

    public void setStatusText(final String statusText) {
        this.statusTextElement.setText(statusText);
    }

    public void rotateCompass(final float angle) {
        this.compassItem.setRotation(new Quaternionf(new AxisAngle4f(180f + angle, 0f, 0f ,0f)));
    }

    @Override
    public SceneElement[] getSceneElements() {
        return this.sceneElements;
    }

    public void updateSize(final Window window) {
        this.statusTextElement.setPosition(10f, window.getHeight() - 50f, 0);
        this.compassItem.setPosition(window.getWidth() - 40f, 50f, 0);
    }
}
