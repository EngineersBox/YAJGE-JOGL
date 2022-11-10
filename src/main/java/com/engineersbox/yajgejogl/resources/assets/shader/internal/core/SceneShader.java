package com.engineersbox.yajgejogl.resources.assets.shader.internal.core;

import com.engineersbox.yajgejogl.rendering.RenderContext;
import com.engineersbox.yajgejogl.rendering.scene.lighting.DirectionalLight;
import com.engineersbox.yajgejogl.rendering.scene.lighting.PointLight;
import com.engineersbox.yajgejogl.rendering.scene.lighting.SpotLight;
import com.engineersbox.yajgejogl.rendering.scene.shadow.ShadowCascade;
import com.engineersbox.yajgejogl.rendering.shader.RenderHandler;
import com.engineersbox.yajgejogl.rendering.shader.ShaderRenderHandler;
import com.engineersbox.yajgejogl.rendering.shader.ShaderStage;
import com.engineersbox.yajgejogl.resources.assets.shader.Shader;
import com.engineersbox.yajgejogl.resources.assets.shader.internal.InstancedShaderHandler;
import com.engineersbox.yajgejogl.resources.assets.shader.internal.preprocess.DepthShader;
import com.engineersbox.yajgejogl.resources.config.io.ConfigHandler;
import com.engineersbox.yajgejogl.scene.lighting.SceneLight;
import com.engineersbox.yajgejogl.util.UniformUtils;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLUniformData;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;
import java.util.stream.Stream;

@RenderHandler(
        name = SceneShader.SHADER_NAME,
        priority = 0,
        stage = ShaderStage.CORE
)
public class SceneShader extends ShaderRenderHandler {

    public static final String SHADER_NAME = "@c4610__internal__SCENE";

    public SceneShader(final GL2 gl) {
        super(
                gl,
                new Shader(gl)
                        .withVertex("shaders/lighting/final.vert")
                        .withFragment("shaders/lighting/final.frag")
        );
        super.shader.build();
        super.shader.createMaterialUniform("material");
        super.shader.createPointLightListUniform("pointLights", SceneLight.MAX_POINT_LIGHTS);
        super.shader.createSpotLightListUniform("spotLights", SceneLight.MAX_SPOT_LIGHTS);
        super.shader.createDirectionalLightUniform("directionalLight");
        super.shader.createFogUniform("fog");
        Stream.of(
                "viewMatrix",
                "projectionMatrix",
                "textureSampler",
                "normalMap",
                "specularPower",
                "ambientLight",
                "modelNonInstancedMatrix",
                "renderShadow",
                "jointsMatrix",
                "isInstanced",
                "cols",
                "rows",
                "selectedNonInstanced",
                "zFar",
                "shadowsOnly",
                "depthOnly",
                "showCascades",
                "debug"
        ).forEach(super.shader::createUniform);
        for (int i = 0; i < DepthShader.NUM_CASCADES; i++) {
            super.shader.createUniform("shadowMap_" + i);
        }
        super.shader.createUniform("orthoProjectionMatrix", DepthShader.NUM_CASCADES);
        super.shader.createUniform("lightViewMatrix", DepthShader.NUM_CASCADES);
        super.shader.createUniform("cascadeFarPlanes", DepthShader.NUM_CASCADES);
    }

    @Override
    public void render(final RenderContext context) {
        super.gl.glViewport(0, 0, context.window().getWidth(), context.window().getHeight());
        context.window().updateProjectionMatrix();
        super.shader.bind();
        super.shader.setUniform(UniformUtils.from("debug", ConfigHandler.CONFIG.engine.debug.flat));
        super.shader.setUniform(UniformUtils.from("shadowsOnly", ConfigHandler.CONFIG.engine.debug.shadowsOnly));
        super.shader.setUniform(UniformUtils.from("depthOnly", ConfigHandler.CONFIG.engine.debug.depthOnly));
        super.shader.setUniform(UniformUtils.from("showCascades", ConfigHandler.CONFIG.engine.debug.showCascades));
        super.shader.setUniform(new GLUniformData("zFar", (float) ConfigHandler.CONFIG.render.camera.zFar));
        super.shader.setUniform(UniformUtils.from(
                "viewMatrix",
                context.camera().getViewMatrix()
        ));
        super.shader.setUniform(UniformUtils.from(
                "projectionMatrix",
                context.window().getProjectionMatrix()
        ));

        final List<ShadowCascade> shadowCascades = context.depthShader().getShadowCascades();
        for (int i = 0; i < DepthShader.NUM_CASCADES; i++) {
            final ShadowCascade shadowCascade = shadowCascades.get(i);
            super.shader.setUniform(UniformUtils.from("orthoProjectionMatrix", shadowCascade.getOrthoProjMatrix(), i));
            super.shader.setUniform(new GLUniformData("cascadeFarPlanes[" + i + "]", DepthShader.CASCADE_SPLITS[i]));
            super.shader.setUniform(UniformUtils.from("lightViewMatrix", shadowCascade.getLightViewMatrix(), i));
        }

        renderLights(context);

        super.shader.setUniform("fog", context.scene().getFog());
        super.shader.setUniform(new GLUniformData("textureSampler", 0));
        super.shader.setUniform(new GLUniformData("normalMap", 1));
        final int start = 2;
        for (int i = 0; i < DepthShader.NUM_CASCADES; i++) {
            super.shader.setUniform(new GLUniformData("shadowMap_" + i, start + i));
        }
        super.shader.setUniform(UniformUtils.from("renderShadow", context.scene().shadowsEnabled()));

        InstancedShaderHandler.renderNonInstancedMeshes(super.shader, context);
        InstancedShaderHandler.renderInstancedMeshes(super.shader, context);
        super.shader.unbind();
    }

    private void renderLights(final RenderContext context) {
        final Matrix4f viewMatrix = context.camera().getViewMatrix();
        final SceneLight sceneLight = context.scene().getSceneLight();
        super.shader.setUniform(UniformUtils.from(
                "ambientLight",
                sceneLight.getAmbientLight()
        ));
        super.shader.setUniform(new GLUniformData(
                "specularPower",
                context.specularPower()
        ));
        final PointLight[] pointLightList = sceneLight.getPointLights();
        int numLights = pointLightList != null ? pointLightList.length : 0;
        for (int i = 0; i < numLights; i++) {
            final PointLight currPointLight = new PointLight(pointLightList[i]);
            final Vector3f lightPos = currPointLight.getPosition();
            final Vector4f aux = new Vector4f(lightPos, 1);
            aux.mul(viewMatrix);
            lightPos.x = aux.x;
            lightPos.y = aux.y;
            lightPos.z = aux.z;
            super.shader.setUniform("pointLights", currPointLight, i);
        }
        final SpotLight[] spotLightList = sceneLight.getSpotLights();
        numLights = spotLightList != null ? spotLightList.length : 0;
        for (int i = 0; i < numLights; i++) {
            final SpotLight currSpotLight = new SpotLight(spotLightList[i]);
            final Vector4f dir = new Vector4f(currSpotLight.getConeDirection(), 0);
            dir.mul(viewMatrix);
            currSpotLight.setConeDirection(new Vector3f(dir.x, dir.y, dir.z));

            final Vector3f lightPos = currSpotLight.getPointLight().getPosition();
            final Vector4f aux = new Vector4f(lightPos, 1);
            aux.mul(viewMatrix);
            lightPos.x = aux.x;
            lightPos.y = aux.y;
            lightPos.z = aux.z;

            super.shader.setUniform("spotLights", currSpotLight, i);
        }
        final DirectionalLight currDirLight = new DirectionalLight(sceneLight.getDirectionalLight());
        final Vector4f dir = new Vector4f(currDirLight.getDirection(), 0);
        dir.mul(viewMatrix);
        currDirLight.setDirection(new Vector3f(dir.x, dir.y, dir.z));
        super.shader.setUniform("directionalLight", currDirLight);
    }
}