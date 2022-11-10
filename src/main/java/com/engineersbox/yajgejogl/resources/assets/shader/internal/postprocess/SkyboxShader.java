package com.engineersbox.yajgejogl.resources.assets.shader.internal.postprocess;

import com.engineersbox.yajgejogl.rendering.RenderContext;
import com.engineersbox.yajgejogl.rendering.shader.RenderHandler;
import com.engineersbox.yajgejogl.rendering.shader.ShaderRenderHandler;
import com.engineersbox.yajgejogl.rendering.shader.ShaderStage;
import com.engineersbox.yajgejogl.resources.assets.shader.Shader;
import com.engineersbox.yajgejogl.scene.element.Skybox;
import com.engineersbox.yajgejogl.scene.element.object.composite.Mesh;
import com.engineersbox.yajgejogl.util.UniformUtils;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLUniformData;
import org.joml.Matrix4f;

import java.util.stream.Stream;

@RenderHandler(
        name = SkyboxShader.SHADER_NAME,
        priority = 0,
        stage = ShaderStage.POST_PROCESS
)
public class SkyboxShader extends ShaderRenderHandler {

    public static final String SHADER_NAME = "@c4610__internal__SKYBOX";

    public SkyboxShader(final GL2 gl) {
        super(
                gl,
                new Shader(gl)
                        .withVertex("shaders/skybox/skybox.vert")
                        .withFragment("shaders/skybox/skybox.frag")
        );
        super.shader.build();
        Stream.of(
                "projectionMatrix",
                "viewModelMatrix",
                "textureSampler",
                "ambientLight",
                "colour",
                "hasTexture"
        ).forEach(super.shader::createUniform);
    }

    @Override
    public void render(final RenderContext context) {
        final Skybox skybox = context.scene().getSkybox();
        if (skybox == null) {
            return;
        }
        super.shader.bind();
        super.shader.setUniform(new GLUniformData("textureSampler", 0));
        super.shader.setUniform(UniformUtils.from(
                "projectionMatrix",
                context.window().getProjectionMatrix()
        ));
        final Matrix4f viewMatrix = context.camera().getViewMatrix();
        final float m30 = viewMatrix.m30();
        viewMatrix.m30(0);
        final float m31 = viewMatrix.m31();
        viewMatrix.m31(0);
        final float m32 = viewMatrix.m32();
        viewMatrix.m32(0);
        final Mesh mesh = skybox.getMesh();
        final Matrix4f viewModelMatrix = context.transform().buildViewModelMatrix(skybox, viewMatrix);
        super.shader.setUniform(UniformUtils.from(
                "viewModelMatrix",
                viewModelMatrix
        ));
        super.shader.setUniform(UniformUtils.from(
                "ambientLight",
                context.scene().getSceneLight().getSkyboxLight()
        ));
        super.shader.setUniform(UniformUtils.from(
                "colour",
                mesh.getMaterial().getAmbientColour()
        ));
        super.shader.setUniform(UniformUtils.from(
                "hasTexture",
                mesh.getMaterial().getAlbedo() != null
        ));
        mesh.render();
        viewMatrix.m30(m30);
        viewMatrix.m31(m31);
        viewMatrix.m32(m32);
        super.shader.unbind();
    }

}
