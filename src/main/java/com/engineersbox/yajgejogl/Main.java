package com.engineersbox.yajgejogl;

import com.engineersbox.yajgejogl.core.Engine;
import com.engineersbox.yajgejogl.core.IGameLogic;
import com.engineersbox.yajgejogl.debug.OpenGLInfo;
import com.engineersbox.yajgejogl.core.Window;
import com.engineersbox.yajgejogl.debug.Overlay;
import com.engineersbox.yajgejogl.debug.Statistics;
import com.engineersbox.yajgejogl.input.MouseInput;
import com.engineersbox.yajgejogl.rendering.Renderer;
import com.engineersbox.yajgejogl.rendering.scene.lighting.DirectionalLight;
import com.engineersbox.yajgejogl.rendering.view.Camera;
import com.engineersbox.yajgejogl.resources.assets.material.Material;
import com.engineersbox.yajgejogl.resources.assets.material.Texture;
import com.engineersbox.yajgejogl.resources.loader.OBJLoader;
import com.engineersbox.yajgejogl.scene.Scene;
import com.engineersbox.yajgejogl.scene.element.SceneElement;
import com.engineersbox.yajgejogl.scene.element.Skybox;
import com.engineersbox.yajgejogl.scene.element.Terrain;
import com.engineersbox.yajgejogl.scene.element.object.composite.HeightMapMesh;
import com.engineersbox.yajgejogl.scene.element.object.composite.Mesh;
import com.engineersbox.yajgejogl.scene.element.particles.FlowParticleEmitter;
import com.engineersbox.yajgejogl.scene.element.particles.Particle;
import com.engineersbox.yajgejogl.scene.lighting.SceneLight;
import com.engineersbox.yajgejogl.util.IteratorUtils;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.util.FPSAnimator;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.*;

public class Main implements IGameLogic, KeyListener {

    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    public static void main(final String[] args) {
        try {
            final IGameLogic gameLogic = new Main();
            final Engine engine = new Engine("GAME", gameLogic);
        } catch (final Exception e) {
            Main.LOGGER.error(e);
            System.exit(1);
        }
    }

    private static final float MOUSE_SENSITIVITY = 0.2f;
    private static final float CAMERA_POS_STEP = 0.05f;
    private static final float CAMERA_ACCELERATION_FACTOR = 1.8f;
    private static final float SKYBOX_SCALE = 10.0f;

    private static final float TERRAIN_SCALE = 10;
    private static final int TERRAIN_SIZE = 3;
    private static final float TERRAIN_MIN_Y = -0.1f;
    private static final float TERRAIN_MAX_Y = 0.1f;
    private static final int TERRAIN_TEX_INC = 40;

    private final Vector3f cameraInc;
    private final Renderer renderer;
    private final Camera camera;
    private float accelerationMultiplier;
    private final Scene scene;
    private SceneLight sceneLight;
    private Terrain terrain;
    private Terrain water;
    private float lightAngle;
    private float angleInc;
    private Hud hud;
    private FlowParticleEmitter particleEmitter;
    private Statistics stats;
    private Overlay debugOverlay;
    private GenerateHeightMap ghm;

    public Main() {
        this.camera = new Camera();
        this.renderer = new Renderer();
        this.cameraInc = new Vector3f(0.0f, 0.0f, 0.0f);
        this.lightAngle = 45;
        this.angleInc = 0;
        this.accelerationMultiplier = 1.0f;
        this.scene = new Scene();
        setupLights();
    }

    @Override
    public void init(final GL2 gl,
                     final Window window) {
        // TODO: FIX HUD
//        this.hud = new Hud(gl, "DEMO");
        this.stats = new Statistics(gl);
        this.stats.init();
        this.renderer.init(gl, window, this.hud);
        window.addKeyListener(this);
        this.ghm = new GenerateHeightMap();

        this.water = new Terrain(
                gl,
                Main.TERRAIN_SIZE,
                Main.TERRAIN_SCALE,
                Main.TERRAIN_MIN_Y,
                Main.TERRAIN_MAX_Y,
                ghm.GenerateHeightMap(69420, 0, "src/main/resources/textures/water_noise.png"),
                "textures/water_alt.png",
                Main.TERRAIN_TEX_INC
        );
//        Arrays.stream(this.water.getSceneElements()).forEach((final SceneElement element) -> {
//            final Vector3f position = element.getPosition();
//            element.setPosition(
//                    position.x,
//                    position.y - 1,
//                    position.z
//            );
//        });

        this.terrain = new Terrain(
                gl,
                Main.TERRAIN_SIZE,
                Main.TERRAIN_SCALE,
                Main.TERRAIN_MIN_Y,
                Main.TERRAIN_MAX_Y,
                "textures/heightmap.png",
                "textures/terrain.png",
                Main.TERRAIN_TEX_INC
        );

        final Skybox skyBox = new Skybox(
                gl,
                "models/skybox.obj",
                "textures/skybox.png"
        );
        skyBox.setScale(Main.SKYBOX_SCALE);
        this.scene.setSkybox(skyBox);

        final SceneElement block = loadElement(gl, "models/cube.obj", "textures/grassblock.png", 1.0f);
        block.setScale(0.5f);
        final SceneElement oakTree = loadElement(gl, "models/trees/oak/oak.obj", "models/trees/oak/oak.jpg", 0.2f);
        oakTree.setScale(0.002f);
        final List<SceneElement> elements = new ArrayList<>();
        elements.addAll(Arrays.asList(this.terrain.getSceneElements()));
        elements.addAll(Arrays.asList(this.water.getSceneElements()));
        elements.addAll(Arrays.asList(createTerrainTrees(gl)));
        this.scene.setSceneElements(elements.toArray(SceneElement[]::new));

        final int maxParticles = 200;
        final Vector3f particleSpeed = new Vector3f(0, 1, 0);
        particleSpeed.mul(2.5f);
        final long ttl = 4000;
        final long creationPeriodMillis = 300;
        final float range = 0.2f;
        final float scale = 1.0f;
        final Mesh partMesh = OBJLoader.load(gl, "models/particle.obj", maxParticles);
        final Texture particleTexture = new Texture(gl, "textures/particle_anim.png", 4, 4);
        final Material partMaterial = new Material(particleTexture, 1f);
        partMesh.setMaterial(partMaterial);
        final Particle particle = new Particle(partMesh, particleSpeed, ttl, 100);
        particle.setScale(scale);
        this.particleEmitter = new FlowParticleEmitter(particle, maxParticles, creationPeriodMillis);
        this.particleEmitter.setActive(true);
        this.particleEmitter.setPositionRndRange(range);
        this.particleEmitter.setSpeedRndRange(range);
        this.particleEmitter.setAnimRange(10);
//        this.scene.setParticleEmitters(new FlowParticleEmitter[]{this.particleEmitter});
        this.scene.setRenderShadows(true);

//        this.scene.setFog(new Fog(
//                true,
//                new Vector3f(0.2f, 0.2f, 0.2f),
//                0.1f
//        ));

        this.camera.getPosition().x = 0.0f;
        this.camera.getPosition().z = 0.0f;
        this.camera.getPosition().y = -0.2f;
        this.camera.getRotation().x = 10.f;

//        final Mesh bunny = OBJLoader.load(gl, "models/bunny.obj");
//        final QuadricErrorSimplifier qes = new QuadricErrorSimplifier(bunny);
//        final Mesh simpleBunny = qes.simplify(gl, 0.25f, 6, false);
    }

    private SceneElement loadElement(final GL2 gl,
                                     final String model,
                                     final String texture,
                                     final float reflectance) {
        final Mesh mesh = OBJLoader.load(gl, model);
        final Texture tex = new Texture(gl, texture);
        final Material material = new Material(tex, reflectance);
        mesh.setMaterial(material);
        mesh.setBoundingRadius(10.5f);
        final SceneElement tree = new SceneElement(mesh);
        tree.setFrustumCulling(true);
        return tree;
    }

    private static final Map<String, Integer> TREES = Map.of(
            "models/trees/ash/ash", 20,
            "models/trees/maple/maple", 20,
            "models/trees/oak/oak", 20,
            "models/trees/pine/pine", 20,
            "models/trees/plum/plum", 20,
            "models/trees/poplar/poplar", 20
    );

    public double randRange(float min, float max) {
        Random random = new Random();
        return random.nextDouble(max - min) + min;
    }

    private SceneElement[] createTerrainTrees(final GL2 gl) {
        final List<SceneElement> trees = new ArrayList<>();
        final Pair<Vector2f, Vector2f> positions = this.terrain.positions();
        IteratorUtils.forEach(TREES, (final String file, final Integer count) -> {
            for (int i = 0; i < count; i++) {
                final Mesh mesh = OBJLoader.load(gl, file + ".obj");
                final Texture tex = new Texture(gl, file + ".jpg");
                final Material material = new Material(tex, 0.2f);
                mesh.setMaterial(material);
                mesh.setBoundingRadius(10.5f);
                final SceneElement tree = new SceneElement(mesh);
                tree.setFrustumCulling(false);
                tree.setScale(0.002f);
                final float x = (float) randRange(positions.getKey().x, positions.getValue().x);
                final float z = (float) randRange(positions.getKey().y, positions.getValue().y);
                final float y = this.terrain.getHeight(new Vector3f(x, 0, z));
                tree.setPosition(x, y, z);
                tree.setRotation(new Quaternionf(new AxisAngle4f((float) (Math.PI / -2), 1, 0, 0)));
                trees.add(tree);
            }
        });
        return trees.toArray(SceneElement[]::new);
    }

    private void setupLights() {
        this.sceneLight = new SceneLight();
        this.scene.setSceneLight(sceneLight);
        this.sceneLight.setAmbientLight(new Vector3f(0.3f, 0.3f, 0.3f));
        this.sceneLight.setSkyboxLight(new Vector3f(1.0f, 1.0f, 1.0f));

        final float lightIntensity = 1.0f;
        final Vector3f lightDirection = new Vector3f(0, 1, 1);
        final DirectionalLight directionalLight = new DirectionalLight(new Vector3f(1, 1, 1), lightDirection, lightIntensity);
        this.sceneLight.setDirectionalLight(directionalLight);
    }

    @Override
    public void input(final Window window,
                      final MouseInput mouseInput) {
    }

    @Override
    public void resize(final Window window) {

    }

    @Override
    public void update(final GL2 gl,
                       final float interval,
                       final MouseInput mouseInput,
                       final Window window) {
        if (mouseInput.isRightButtonPressed()) {
            final Vector2f rotation = mouseInput.getDisplayVec();
            this.camera.moveRotation(
                    rotation.x * Main.MOUSE_SENSITIVITY,
                    rotation.y * Main.MOUSE_SENSITIVITY,
                    0
            );
        }
        final Vector3f prevPos = new Vector3f(this.camera.getPosition());
        this.camera.movePosition(
                this.cameraInc.x * Main.CAMERA_POS_STEP * this.accelerationMultiplier,
                this.cameraInc.y * Main.CAMERA_POS_STEP * this.accelerationMultiplier,
                this.cameraInc.z * Main.CAMERA_POS_STEP * this.accelerationMultiplier
        );
        final float height = this.terrain.getHeight(this.camera.getPosition());
        if (this.camera.getPosition().y <= height) {
            this.camera.setPosition(prevPos.x, prevPos.y, prevPos.z);
        }
        this.camera.updateViewMatrix();

        this.lightAngle = Math.min(Math.max(this.lightAngle + this.angleInc, 0), 180);
        final Vector3f lightDirection = this.scene.getSceneLight().getDirectionalLight().getDirection();
        lightDirection.x = 0;
        lightDirection.y = (float) Math.sin(Math.toRadians(this.lightAngle));
        lightDirection.z = (float) Math.cos(Math.toRadians(this.lightAngle));
        lightDirection.normalize();

        this.particleEmitter.update((long) (interval * 1000));
        this.camera.updateViewMatrix();
    }

    @Override
    public void render(final GL2 gl,
                       final Window window) {
        this.stats.begin();
        this.renderer.render(window, this.camera, this.scene);
        this.stats.end();
//        this.hud.updateSize(window);
    }

    @Override
    public void cleanup() {
        this.scene.getMeshSceneElements()
                .keySet()
                .forEach(Mesh::cleanUp);
//        this.hud.cleanup();
    }

    @Override
    public void keyTyped(final KeyEvent e) {

    }

    @SuppressWarnings("java:S131")
    @Override
    public void keyPressed(final KeyEvent event) {
        final int keyCode = event.getKeyCode();
//        Main.LOGGER.trace("[KEY EVENT] Pressed: [Code: {}] [Char: {}]", keyCode, event.getKeyChar());
        switch (keyCode) {
            case KeyEvent.VK_W -> this.cameraInc.z = -1;
            case KeyEvent.VK_S -> this.cameraInc.z = 1;
            case KeyEvent.VK_A -> this.cameraInc.x = -1;
            case KeyEvent.VK_D -> this.cameraInc.x = 1;
            case KeyEvent.VK_SHIFT -> this.cameraInc.y = -1;
            case KeyEvent.VK_SPACE -> this.cameraInc.y = 1;
            case KeyEvent.VK_LEFT -> this.angleInc -= 0.05f;
            case KeyEvent.VK_RIGHT -> this.angleInc += 0.05f;
        }
        this.accelerationMultiplier = event.isControlDown() ? Main.CAMERA_ACCELERATION_FACTOR : 1.0f;
    }

    @SuppressWarnings("java:S131")
    @Override
    public void keyReleased(final KeyEvent event) {
        final int keyCode = event.getKeyCode();
//        Main.LOGGER.trace("[KEY EVENT] Released: [Code: {}] [Char: {}]", keyCode, event.getKeyChar());
        switch (keyCode) {
            case KeyEvent.VK_W, KeyEvent.VK_S -> this.cameraInc.z = 0;
            case KeyEvent.VK_A, KeyEvent.VK_D -> this.cameraInc.x = 0;
            case KeyEvent.VK_SHIFT, KeyEvent.VK_SPACE -> this.cameraInc.y = 0;
            case KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT -> this.angleInc = 0;
        }
    }

    @Override
    public void debugInfo(final GL2 gl,
                           final GLAutoDrawable glAutoDrawable,
                           final Window window,
                           final OpenGLInfo info,
                           final FPSAnimator animator) {
        if (this.debugOverlay == null) {
            this.debugOverlay = new Overlay(
                    12,
                    window,
                    info,
                    animator,
                    this.camera,
                    this.stats,
                    this.renderer,
                    this.scene
            );
        }
        this.debugOverlay.render();
    }
}
