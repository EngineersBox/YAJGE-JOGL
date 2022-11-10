package com.engineersbox.yajgejogl.rendering;

import com.engineersbox.yajgejogl.core.Window;
import com.engineersbox.yajgejogl.rendering.shader.RenderHandler;
import com.engineersbox.yajgejogl.rendering.shader.ShaderRenderHandler;
import com.engineersbox.yajgejogl.rendering.shader.ShaderStage;
import com.engineersbox.yajgejogl.rendering.view.Camera;
import com.engineersbox.yajgejogl.rendering.view.Transform;
import com.engineersbox.yajgejogl.rendering.view.culling.FrustumCullingFilter;
import com.engineersbox.yajgejogl.resources.assets.shader.Shader;
import com.engineersbox.yajgejogl.resources.assets.shader.ShadowMap;
import com.engineersbox.yajgejogl.resources.assets.shader.internal.core.SceneShader;
import com.engineersbox.yajgejogl.resources.assets.shader.internal.preprocess.DepthShader;
import com.engineersbox.yajgejogl.resources.config.io.ConfigHandler;
import com.engineersbox.yajgejogl.scene.Scene;
import com.engineersbox.yajgejogl.scene.element.SceneElement;
import com.engineersbox.yajgejogl.scene.gui.IHud;
import com.engineersbox.yajgejogl.util.StreamUtils;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.util.gl2.GLUT;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Renderer implements RenderingElement {

    private static final Logger LOGGER = LogManager.getLogger(Renderer.class);
    private static final Reflections SHADER_RENDER_HANDLER_REFLECTIONS = new Reflections(new ConfigurationBuilder()
            .addScanners(Scanners.TypesAnnotated, Scanners.SubTypes)
            .forPackages("com.engineersbox.yajgejogl")
    );

    private LinkedMap<String, ShaderRenderHandler> preProcessRenderHandlers;
    private LinkedMap<String, ShaderRenderHandler> coreRenderHandlers;
    private LinkedMap<String, ShaderRenderHandler> postProcessRenderHandlers;
    private final Transform transform;
    private ShadowMap shadowMap;
    private final FrustumCullingFilter frustumCullingFilter;
    private final List<SceneElement> filteredSceneElements;
    private final float specularPower;
    private IHud hud;
    private GL2 gl;

    public Renderer() {
        this.preProcessRenderHandlers = new LinkedMap<>();
        this.coreRenderHandlers = new LinkedMap<>();
        this.postProcessRenderHandlers = new LinkedMap<>();
        this.transform = new Transform();
        this.filteredSceneElements = new ArrayList<>();
        this.frustumCullingFilter = new FrustumCullingFilter();
        this.specularPower = 10.0f;
    }

    public void init(final GL2 gl,
                     final Window window,
                     final IHud hud) {
        this.gl = gl;
        this.shadowMap = new ShadowMap(gl);
        this.hud = hud;
        resolveShaders();
    }

    public List<SceneElement> getFilteredSceneElements() {
        return this.filteredSceneElements;
    }

    private void resolveShaders() {
        final Set<Class<? extends ShaderRenderHandler>> renderHandlers = Renderer.SHADER_RENDER_HANDLER_REFLECTIONS.getSubTypesOf(ShaderRenderHandler.class);
        final Set<Class<? extends ShaderRenderHandler>> handlers = renderHandlers.stream()
                .filter((final Class<? extends ShaderRenderHandler> clazz) -> clazz.isAnnotationPresent(RenderHandler.class))
                .collect(Collectors.toSet());
        Renderer.LOGGER.info("Found {} shader render handlers (pre-classification)", handlers.size());
        this.preProcessRenderHandlers = resolveShaderHandlerMapForStream(filterShaderStage(handlers.stream(), ShaderStage.PRE_PROCESS));
        this.coreRenderHandlers = resolveShaderHandlerMapForStream(filterShaderStage(handlers.stream(), ShaderStage.CORE));
        this.postProcessRenderHandlers = resolveShaderHandlerMapForStream(filterShaderStage(handlers.stream(), ShaderStage.POST_PROCESS));
    }

    private LinkedMap<String, ShaderRenderHandler> resolveShaderHandlerMapForStream(final Stream<Class<? extends ShaderRenderHandler>> handlers) {
        return handlers.peek((final Class<? extends ShaderRenderHandler> handler) -> {
                    final RenderHandler annotation = handler.getAnnotation(RenderHandler.class);
                    Renderer.LOGGER.info(
                            "[SHADER STAGE: {}] Found shader render handler \"{}\" with priority {}",
                            annotation.stage(),
                            annotation.name(),
                            annotation.priority()
                    );
                }).map((final Class<? extends ShaderRenderHandler> clazz) -> {
                    try {
                        final Constructor<? extends ShaderRenderHandler> constructor = clazz.getDeclaredConstructor(GL2.class);
                        return (ShaderRenderHandler) constructor.newInstance(this.gl);
                    } catch (final IllegalAccessException | NoSuchMethodException | InvocationTargetException | InstantiationException e) {
                        throw new GLException(String.format(
                                "Unable to instantiate shader %s render handler:",
                                clazz.getAnnotation(RenderHandler.class).name()
                        ), e);
                    }
                }).collect(Collectors.toMap(
                        (final ShaderRenderHandler handler) -> handler.getClass().getAnnotation(RenderHandler.class).name(),
                        Function.identity(),
                        (final ShaderRenderHandler handler1, final ShaderRenderHandler handler2) -> {
                            throw new IllegalStateException("Unexpected duplicate shader: " + handler1.getClass().getAnnotation(RenderHandler.class).name());
                        }, LinkedMap::new
                ));
    }

    private static Stream<Class<? extends ShaderRenderHandler>> filterShaderStage(final Stream<Class<? extends ShaderRenderHandler>> handlerStream,
                                                                                  final ShaderStage stage) {
        return handlerStream.filter((final Class<? extends ShaderRenderHandler> clazz) -> {
            final RenderHandler annotation = clazz.getAnnotation(RenderHandler.class);
            return annotation.stage().equals(stage);
        }).sorted((final Class<? extends ShaderRenderHandler> handler1, final Class<? extends ShaderRenderHandler> handler2) -> {
            final RenderHandler handlerAnnotation1 = handler1.getAnnotation(RenderHandler.class);
            final RenderHandler handlerAnnotation2 = handler2.getAnnotation(RenderHandler.class);
            return Integer.compare(handlerAnnotation1.priority(), handlerAnnotation2.priority());
        });
    }

    public void render(final Window window,
                       final Camera camera,
                       final Scene scene) {
        this.gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT | GL2.GL_STENCIL_BUFFER_BIT);
        if (ConfigHandler.CONFIG.render.camera.frustrumCulling) {
            this.frustumCullingFilter.updateFrustum(window.getProjectionMatrix(), camera.getViewMatrix());
            this.frustumCullingFilter.filter(scene.getMeshSceneElements());
            this.frustumCullingFilter.filter(scene.getInstancedMeshSceneElements());
        }
        final ShaderRenderHandler depthShader = this.preProcessRenderHandlers.get(DepthShader.SHADER_NAME);
        final RenderContext context = new RenderContext(
                window,
                camera,
                scene,
                this.filteredSceneElements,
                this.transform,
                this.shadowMap,
                this.specularPower,
                (DepthShader) depthShader,
                this.hud
        );
        StreamUtils.zipForEach(
                Arrays.stream(ShaderStage.values()),
                Stream.of(
                        this.preProcessRenderHandlers,
                        this.coreRenderHandlers,
                        this.postProcessRenderHandlers
                ),
                (final ShaderStage stage, final LinkedMap<String, ShaderRenderHandler> handlers) -> {
//                    Renderer.LOGGER.info("Executing {} shaders", stage);
                    handlers.forEach(createRenderHandlerConsumer(stage, context, gl));
                }
        );
        if (ConfigHandler.CONFIG.engine.debug.showAxis) {
            renderAxes(camera);
        }
    }

    private static BiConsumer<String, ShaderRenderHandler> createRenderHandlerConsumer(final ShaderStage stage,
                                                                                       final RenderContext context,
                                                                                       final GL2 gl) {
        return (final String name, final ShaderRenderHandler handler) -> {
            final Shader shader = handler.provideShader();
            if (!shader.isValid()) {
                Renderer.LOGGER.warn("[Shader: {}] Invalid failed during link or validation, skipping {}", shader.program(), name);
                return;
            }
//            Renderer.LOGGER.debug("[Shader: {}] Invoking {} shader {}", shader.program(), stage, name);
            handler.render(context);
        };
    }

    private void renderAxes(final Camera camera) {
        final GLUT glut = new GLUT();
        this.gl.glPushMatrix();
        this.gl.glTranslatef(0, 0, 0);
        this.gl.glLoadIdentity();
        final float rotX = camera.getRotation().x;
        final float rotY = camera.getRotation().y;
        final float rotZ = 0;
        this.gl.glRotatef(rotX, 1.0f, 0.0f, 0.0f);
        this.gl.glRotatef(rotY, 0.0f, 1.0f, 0.0f);
        this.gl.glRotatef(rotZ, 0.0f, 0.0f, 1.0f);
        this.gl.glLineWidth(2.0f);

        this.gl.glBegin(GL2.GL_LINES);
        // X Axis
        this.gl.glColor3f(1.0f, 0.0f, 0.0f);
        this.gl.glVertex3f(0.0f, 0.0f, 0.0f);
        this.gl.glVertex3f(0.04f, 0.0f, 0.0f);

        // Y Axis
        this.gl.glColor3f(0.0f, 1.0f, 0.0f);
        this.gl.glVertex3f(0.0f, 0.0f, 0.0f);
        this.gl.glVertex3f(0.0f, 0.04f, 0.0f);

        // Z Axis
        this.gl.glColor3f(0.0f, 0.0f, 1.0f);
        this.gl.glVertex3f(0.0f, 0.0f, 0.0f);
        this.gl.glVertex3f(0.0f, 0.0f, 0.04f);

        this.gl.glEnd();

        if (ConfigHandler.CONFIG.engine.glOptions.cullface) {
            this.gl.glDisable(GL2.GL_CULL_FACE);
        }

        // Y axis
        this.gl.glPushMatrix();
        this.gl.glColor3f(1.0f, 0.0f, 0.0f);
        this.gl.glRotatef(90.0f, 0.0f,1.0f,0.0f);
        this.gl.glTranslatef(0.0f, 0.0f, 0.04f);
        glut.glutSolidCone(0.007,0.025, 20, 20);
        this.gl.glPopMatrix();

        // X axis
        this.gl.glPushMatrix();
        this.gl.glColor3f(0.0f, 0.0f, 1.0f);
        this.gl.glRotatef(90.0f, 0.0f,0.0f,1.0f);
        this.gl.glTranslatef(0.0f, 0.00f, 0.04f);
        glut.glutSolidCone(0.007,0.025, 20, 20);
        this.gl.glPopMatrix();

        // Z axis
        this.gl.glPushMatrix();
        this.gl.glColor3f(0.0f, 1.0f, 0.0f);
        this.gl.glRotatef(90.0f, -1.0f,0.0f,0.0f);
        this.gl.glTranslatef(0.0f, 0.00f, 0.04f);
        glut.glutSolidCone(0.007,0.025, 20, 20);
        this.gl.glPopMatrix();

        if (ConfigHandler.CONFIG.engine.glOptions.cullface) {
            this.gl.glEnable(GL2.GL_CULL_FACE);
        }
        this.gl.glPopMatrix();

    }

    @Override
    public void update(final Window window) {
    }
}
