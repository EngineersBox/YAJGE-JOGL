package com.engineersbox.yajgejogl.resources.assets.shader.internal.postprocess;

import com.engineersbox.yajgejogl.rendering.RenderContext;
import com.engineersbox.yajgejogl.rendering.shader.RenderHandler;
import com.engineersbox.yajgejogl.rendering.shader.ShaderRenderHandler;
import com.engineersbox.yajgejogl.rendering.shader.ShaderStage;
import com.engineersbox.yajgejogl.resources.assets.material.Texture;
import com.engineersbox.yajgejogl.resources.assets.shader.Shader;
import com.engineersbox.yajgejogl.scene.element.object.composite.InstancedMesh;
import com.engineersbox.yajgejogl.scene.element.particles.IParticleEmitter;
import com.engineersbox.yajgejogl.util.UniformUtils;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLUniformData;
import org.joml.Matrix4f;

import java.util.stream.Stream;

@RenderHandler(
        name = ParticleShader.SHADER_NAME,
        priority = 1,
        stage = ShaderStage.POST_PROCESS
)
public class ParticleShader extends ShaderRenderHandler {

    public static final String SHADER_NAME = "@c4610__internal__PARTICLE";

    public ParticleShader(final GL2 gl) {
        super(
                gl,
                new Shader(gl)
                        .withVertex("shaders/particles/particles.vert")
                        .withFragment("shaders/particles/particles.frag")
        );
        super.shader.build();
        Stream.of(
                "projectionMatrix",
                "textureSampler",
                "cols",
                "rows"
        ).forEach(super.shader::createUniform);
    }

    @Override
    public void render(final RenderContext context) {
        super.shader.bind();
        final Matrix4f viewMatrix = context.camera().getViewMatrix();
        super.shader.setUniform(UniformUtils.from(
                "viewMatrix",
                viewMatrix
        ));
        super.shader.setUniform(new GLUniformData("textureSampler", 0));
        final Matrix4f projectionMatrix = context.window().getProjectionMatrix();
        super.shader.setUniform(UniformUtils.from(
                "projectionMatrix",
                projectionMatrix
        ));
        final IParticleEmitter[] emitters = context.scene().getParticleEmitters();
        if (emitters == null) {
            super.gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
            super.gl.glDepthMask(true);
            super.shader.unbind();
            return;
        }

        super.gl.glDepthMask(false);
        super.gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE);

        for (final IParticleEmitter emitter : emitters) {
            final InstancedMesh mesh = (InstancedMesh) emitter.getBaseParticle().getMesh();
            final Texture texture = mesh.getMaterial().getAlbedo();
            super.shader.setUniform(new GLUniformData("cols", texture.getCols()));
            super.shader.setUniform(new GLUniformData("rows", texture.getRows()));
            mesh.renderListInstanced(
                    emitter.getParticles(),
                    true,
                    context.transform(),
                    viewMatrix
            );
        }
        super.gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
        super.gl.glDepthMask(true);
        super.shader.unbind();
    }
}
