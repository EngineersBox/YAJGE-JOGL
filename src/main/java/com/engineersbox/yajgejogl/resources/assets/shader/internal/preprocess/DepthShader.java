package com.engineersbox.yajgejogl.resources.assets.shader.internal.preprocess;

import com.engineersbox.yajgejogl.rendering.RenderContext;
import com.engineersbox.yajgejogl.rendering.scene.lighting.DirectionalLight;
import com.engineersbox.yajgejogl.rendering.scene.shadow.ShadowBuffer;
import com.engineersbox.yajgejogl.rendering.scene.shadow.ShadowCascade;
import com.engineersbox.yajgejogl.rendering.shader.RenderHandler;
import com.engineersbox.yajgejogl.rendering.shader.ShaderRenderHandler;
import com.engineersbox.yajgejogl.rendering.shader.ShaderStage;
import com.engineersbox.yajgejogl.resources.assets.shader.Shader;
import com.engineersbox.yajgejogl.resources.config.io.ConfigHandler;
import com.engineersbox.yajgejogl.scene.animation.AnimatedSceneElement;
import com.engineersbox.yajgejogl.scene.element.SceneElement;
import com.engineersbox.yajgejogl.scene.element.object.composite.InstancedMesh;
import com.engineersbox.yajgejogl.scene.element.object.composite.Mesh;
import com.engineersbox.yajgejogl.scene.lighting.SceneLight;
import com.engineersbox.yajgejogl.util.UniformUtils;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLUniformData;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@RenderHandler(
        name = DepthShader.SHADER_NAME,
        priority = 0,
        stage = ShaderStage.PRE_PROCESS
)
public class DepthShader extends ShaderRenderHandler {

    public static final String SHADER_NAME = "@c4610__internal__DEPTH";
    public static final int NUM_CASCADES = 3;
    public static final float[] CASCADE_SPLITS = new float[]{
            ((float) ConfigHandler.CONFIG.render.camera.zFar) / 20.0f,
            ((float) ConfigHandler.CONFIG.render.camera.zFar) / 10.0f,
            (float) ConfigHandler.CONFIG.render.camera.zFar
    };
    private List<ShadowCascade> shadowCascades;
    private ShadowBuffer shadowBuffer;
    private final List<SceneElement> filteredItems;

    public DepthShader(final GL2 gl) {
        super(
                gl,
                new Shader(gl)
                        .withVertex("shaders/lighting/depth.vert")
                        .withFragment("shaders/lighting/depth.frag")
        );
        this.filteredItems = new ArrayList<>();
        super.shader.build();
        Stream.of(
                "isInstanced",
                "modelNonInstancedMatrix",
                "lightViewMatrix",
                "jointsMatrix",
                "orthoProjectionMatrix"
        ).forEach(super.shader::createUniform);
        this.shadowBuffer = new ShadowBuffer(gl);
        this.shadowCascades = new ArrayList<>();
        float zNear = (float) ConfigHandler.CONFIG.render.camera.zNear;
        for (int i = 0; i < NUM_CASCADES; i++) {
            final ShadowCascade shadowCascade = new ShadowCascade(zNear, CASCADE_SPLITS[i]);
            this.shadowCascades.add(shadowCascade);
            zNear = CASCADE_SPLITS[i];
        }
    }

    public List<ShadowCascade> getShadowCascades() {
        return this.shadowCascades;
    }

    public void bindTextures(final int start) {
        this.shadowBuffer.bindTextures(start);
    }

    private void update(final RenderContext context) {
        final SceneLight sceneLight = context.scene().getSceneLight();
        final DirectionalLight directionalLight = sceneLight != null ? sceneLight.getDirectionalLight() : null;
        this.shadowCascades.forEach((final ShadowCascade shadowCascade) -> shadowCascade.update(
                context.window(),
                context.camera().getViewMatrix(),
                directionalLight
        ));
    }

    @Override
    public void render(final RenderContext context) {
        if (!context.scene().shadowsEnabled()) {
            return;
        }
        update(context);
        super.gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, this.shadowBuffer.getDepthMapFBO());
        super.gl.glViewport(0, 0, ShadowBuffer.SHADOW_MAP_WIDTH, ShadowBuffer.SHADOW_MAP_HEIGHT);
        super.gl.glClear(GL2.GL_DEPTH_BUFFER_BIT);
        super.shader.bind();

        for (int i = 0; i < NUM_CASCADES; i++) {
            final ShadowCascade shadowCascade = this.shadowCascades.get(i);
            super.shader.setUniform(UniformUtils.from("orthoProjectionMatrix", shadowCascade.getOrthoProjMatrix()));
            super.shader.setUniform(UniformUtils.from("lightViewMatrix", shadowCascade.getLightViewMatrix()));

            super.gl.glFramebufferTexture2D(
                    GL2.GL_FRAMEBUFFER,
                    GL2.GL_DEPTH_ATTACHMENT,
                    GL2.GL_TEXTURE_2D,
                    this.shadowBuffer.getDepthMapTexture().getIds().get(i),
                    0
            );
            super.gl.glClear(GL2.GL_DEPTH_BUFFER_BIT);
            renderNonInstancedMeshes(context);
            renderInstancedMeshes(context);
        }

        super.shader.unbind();
        super.gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
    }

    private void renderNonInstancedMeshes(final RenderContext context) {
        super.shader.setUniform(new GLUniformData("isInstanced", 0));
        for (final Map.Entry<Mesh, List<SceneElement>> entry : context.scene().getMeshSceneElements().entrySet()) {
            entry.getKey().renderList(
                    entry.getValue(),
                    (final SceneElement sceneElement) -> {
                        final Matrix4f modelMatrix = context.transform().buildModelMatrix(sceneElement);
                        super.shader.setUniform(UniformUtils.from(
                                "modelNonInstancedMatrix",
                                modelMatrix
                        ));
                        if (sceneElement instanceof AnimatedSceneElement animatedSceneElement) {
                            super.shader.setUniform(UniformUtils.from(
                                    "jointsMatrix",
                                    animatedSceneElement.getCurrentFrame().getJointMatrices()
                            ));
                        }
                    }
            );
        }
    }

    private void renderInstancedMeshes(final RenderContext context) {
        super.shader.setUniform(new GLUniformData("isInstanced", 1));
        for (final Map.Entry<InstancedMesh, List<SceneElement>> entry : context.scene().getInstancedMeshSceneElements().entrySet()) {
            this.filteredItems.clear();
            entry.getValue()
                    .stream()
                    .filter(SceneElement::isInsideFrustum)
                    .forEach(this.filteredItems::add);
            bindTextures(GL2.GL_TEXTURE2);
            entry.getKey().renderListInstanced(this.filteredItems, context.transform(), null);
        }
    }
}
