package com.engineersbox.yajgejogl.debug;

import com.engineersbox.yajgejogl.core.Window;
import com.engineersbox.yajgejogl.rendering.Renderer;
import com.engineersbox.yajgejogl.rendering.view.Camera;
import com.engineersbox.yajgejogl.resources.config.io.ConfigHandler;
import com.engineersbox.yajgejogl.scene.Scene;
import com.engineersbox.yajgejogl.scene.element.SceneElement;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.awt.TextRenderer;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Overlay {

    private final TextRenderer textRenderer;
    private final Window window;
    private final OpenGLInfo info;
    private final FPSAnimator animator;
    private final Camera camera;
    private final Statistics stats;
    private final int fontSize;
    private final Renderer renderer;
    private final Scene scene;

    public Overlay(final int fontSize,
                   final Window window,
                   final OpenGLInfo info,
                   final FPSAnimator animator,
                   final Camera camera,
                   final Statistics stats,
                   final Renderer renderer,
                   final Scene scene) {
        this.fontSize = fontSize;
        this.window = window;
        this.info = info;
        this.animator = animator;
        this.camera = camera;
        this.stats = stats;
        this.renderer = renderer;
        this.scene = scene;
        this.textRenderer =  new TextRenderer(new Font("SansSerif", Font.PLAIN, fontSize));
    }

    public void render() {
        if (!ConfigHandler.CONFIG.engine.debug.overlay) {
            return;
        }
        this.textRenderer.setSmoothing(true);
        final java.util.List<Pair<String, Color>> lines = new ArrayList<>();
        if (ConfigHandler.CONFIG.video.showFps) {
            lines.add(ImmutablePair.of("[FPS] " + animator.getFPS(), Color.MAGENTA));
        }
        lines.add(ImmutablePair.of("[OPENGL CONTEXT]", Color.ORANGE));
        lines.add(ImmutablePair.of("  - Version: " + info.version(), Color.WHITE));
        lines.add(ImmutablePair.of("  - GLSL Version: " + info.glslVersion(), Color.WHITE));
        lines.add(ImmutablePair.of("  - Vendor: " + info.vendor(), Color.WHITE));
        lines.add(ImmutablePair.of("  - Renderer: " + info.renderer(), Color.WHITE));
        lines.add(ImmutablePair.of("  - Extensions: " + info.extensions(), Color.WHITE));
        lines.add(ImmutablePair.of("[ENGINE PROPERTIES]", Color.CYAN));
        lines.add(ImmutablePair.of("  - Cull Faces: " + ConfigHandler.CONFIG.engine.glOptions.cullface, Color.WHITE));
        lines.add(ImmutablePair.of("  - Show Triangles: " + ConfigHandler.CONFIG.engine.glOptions.showTrianges, Color.WHITE));
        lines.add(ImmutablePair.of("  - Compat Profile: " + ConfigHandler.CONFIG.engine.glOptions.compatProfile, Color.WHITE));
        lines.add(ImmutablePair.of("  - Frustum Culling: " + ConfigHandler.CONFIG.render.camera.frustrumCulling, Color.WHITE));
        lines.add(ImmutablePair.of("[DEBUG]", Color.GREEN));
        lines.add(ImmutablePair.of("  - Shadows Only: " + ConfigHandler.CONFIG.engine.debug.shadowsOnly, Color.WHITE));
        lines.add(ImmutablePair.of("  - Depth Only: " + ConfigHandler.CONFIG.engine.debug.depthOnly, Color.WHITE));
        lines.add(ImmutablePair.of("  - Flat: " + ConfigHandler.CONFIG.engine.debug.flat, Color.WHITE));
        lines.add(ImmutablePair.of("  - Show Shadow Cascades: " + ConfigHandler.CONFIG.engine.debug.showCascades, Color.WHITE));
        lines.add(ImmutablePair.of("[CAMERA]", Color.GREEN));
        lines.add(ImmutablePair.of("  - Near/Far: " + ConfigHandler.CONFIG.render.camera.zNear + "/" + ConfigHandler.CONFIG.render.camera.zFar, Color.WHITE));
        lines.add(ImmutablePair.of("  - FOV: " + ConfigHandler.CONFIG.render.camera.fov, Color.WHITE));
        lines.add(ImmutablePair.of("  - Position: [", Color.WHITE));
        lines.add(ImmutablePair.of("       X: " + this.camera.getPosition().x + ",", Color.WHITE));
        lines.add(ImmutablePair.of("       Y: " + this.camera.getPosition().y + ",", Color.WHITE));
        lines.add(ImmutablePair.of("       Z: " + this.camera.getPosition().z + ",", Color.WHITE));
        lines.add(ImmutablePair.of("    ]", Color.WHITE));
        lines.add(ImmutablePair.of("  - Rotation: [", Color.WHITE));
        lines.add(ImmutablePair.of("       X: " + this.camera.getRotation().x + ",", Color.WHITE));
        lines.add(ImmutablePair.of("       Y: " + this.camera.getRotation().y + ",", Color.WHITE));
        lines.add(ImmutablePair.of("       Z: " + this.camera.getRotation().z + ",", Color.WHITE));
        lines.add(ImmutablePair.of("    ]", Color.WHITE));
        lines.add(ImmutablePair.of("[RENDERER]", Color.CYAN));
        lines.add(ImmutablePair.of("  - Pre-Cull Scene Elements:", Color.WHITE));
        lines.add(ImmutablePair.of("    - Instanced: " + this.scene.getInstancedMeshSceneElements()
                .values()
                .stream()
                .mapToLong(List::size)
                .sum() + ",", Color.WHITE));
        lines.add(ImmutablePair.of("    - Non-Instanced: " + this.scene.getMeshSceneElements()
                .values()
                .stream()
                .mapToLong(List::size)
                .sum()+ ",", Color.WHITE));
        lines.add(ImmutablePair.of("  - Culled Scene Elements", Color.WHITE));
        lines.add(ImmutablePair.of("    - Instanced: " + this.scene.getInstancedMeshSceneElements()
                .values()
                .stream()
                .map((final List<SceneElement> elements) -> elements.stream().filter(SceneElement::isInsideFrustum))
                .mapToLong(Stream::count)
                .sum() + ",", Color.WHITE));
        lines.add(ImmutablePair.of("    - Non-Instanced: " + this.scene.getMeshSceneElements()
                .values()
                .stream()
                .map((final List<SceneElement> elements) -> elements.stream().filter(SceneElement::isInsideFrustum))
                .mapToLong(Stream::count)
                .sum()+ ",", Color.WHITE));
        lines.add(ImmutablePair.of("[PIPELINE]", Color.ORANGE));
        for (final Statistics.Stat stat : Statistics.Stat.values()) {
            lines.add(ImmutablePair.of(String.format(
                    "  - %s: %d",
                    stat.name(),
                    this.stats.getResult(stat)
            ), Color.WHITE));
        }
        renderDebugLines(
                window,
                lines,
                5,
                window.getHeight() - 20
        );
    }

    private void renderDebugLines(final Window window,
                                  final List<Pair<String, Color>> lines,
                                  final int xStart,
                                  final int yStart) {
        int yPos = yStart;
        for (final Pair<String, Color> line : lines) {
            this.textRenderer.setColor(line.getValue());
            this.textRenderer.beginRendering(window.getWidth(), window.getHeight());
            this.textRenderer.draw(line.getKey(), xStart, yPos);
            this.textRenderer.endRendering();
            yPos -= this.fontSize + 2;
        }
    }

}
