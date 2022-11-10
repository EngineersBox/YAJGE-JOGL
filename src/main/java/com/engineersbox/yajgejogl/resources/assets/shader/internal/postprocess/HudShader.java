package com.engineersbox.yajgejogl.resources.assets.shader.internal.postprocess;

import com.engineersbox.yajgejogl.rendering.RenderContext;
import com.engineersbox.yajgejogl.rendering.shader.RenderHandler;
import com.engineersbox.yajgejogl.rendering.shader.ShaderRenderHandler;
import com.engineersbox.yajgejogl.rendering.shader.ShaderStage;
import com.engineersbox.yajgejogl.resources.assets.shader.Shader;
import com.engineersbox.yajgejogl.scene.element.SceneElement;
import com.engineersbox.yajgejogl.scene.element.object.composite.Mesh;
import com.engineersbox.yajgejogl.util.UniformUtils;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLUniformData;
import org.joml.Matrix4f;

import java.util.stream.Stream;

@RenderHandler(
        name = HudShader.SHADER_NAME,
        priority = 2,
        stage = ShaderStage.POST_PROCESS
)
public class HudShader extends ShaderRenderHandler {

    public static final String SHADER_NAME = "@c4610__internal__HUD_SHADER";

    public HudShader(final GL2 gl) {
        super(
                gl,
                new Shader(gl)
                        .withVertex("shaders/hud/hud.vert")
                        .withFragment("shaders/hud/hud.frag")
        );
        super.shader.build();
        Stream.of(
                "projModelMatrix",
                "colour",
                "hasTexture"
        ).forEach(super.shader::createUniform);
    }

    @Override
    public void render(final RenderContext context) {
        if (context.hud() == null) {
            return;
        }
        super.shader.bind();
        final Matrix4f orthoProjectionMatrix = context.transform().getOrtho2DProjectionMatrix(
                0,
                context.window().getWidth(),
                context.window().getHeight(),
                0
        );
        for (final SceneElement sceneElement : context.hud().getSceneElements()) {
            final Mesh mesh = sceneElement.getMesh();
            final Matrix4f projModelMatrix = context.transform().buildOrthoProjModelMatrix(sceneElement, orthoProjectionMatrix);
            super.shader.setUniform(UniformUtils.from(
                    "projModelMatrix",
                    projModelMatrix
            ));
            super.shader.setUniform(UniformUtils.from(
                    "colour",
                    sceneElement.getMesh().getMaterial().getAmbientColour()
            ));
            super.shader.setUniform(UniformUtils.from(
                    "hasTexture",
                    sceneElement.getMesh().getMaterial().getAlbedo() != null
            ));
            mesh.render();
        }
        super.shader.unbind();
    }
}
