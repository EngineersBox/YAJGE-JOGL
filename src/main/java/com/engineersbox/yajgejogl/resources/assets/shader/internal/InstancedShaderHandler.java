package com.engineersbox.yajgejogl.resources.assets.shader.internal;

import com.engineersbox.yajgejogl.rendering.RenderContext;
import com.engineersbox.yajgejogl.resources.assets.material.Texture;
import com.engineersbox.yajgejogl.resources.assets.shader.Shader;
import com.engineersbox.yajgejogl.resources.assets.shader.internal.preprocess.DepthShader;
import com.engineersbox.yajgejogl.scene.animation.AnimatedSceneElement;
import com.engineersbox.yajgejogl.scene.element.SceneElement;
import com.engineersbox.yajgejogl.scene.element.object.composite.InstancedMesh;
import com.engineersbox.yajgejogl.scene.element.object.composite.Mesh;
import com.engineersbox.yajgejogl.util.IteratorUtils;
import com.engineersbox.yajgejogl.util.UniformUtils;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLUniformData;
import org.joml.Matrix4f;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class InstancedShaderHandler {

    private InstancedShaderHandler() {
        throw new IllegalStateException("Utility class");
    }

    public static void renderNonInstancedMeshes(final Shader shader,
                                                final RenderContext context) {
        shader.setUniform(UniformUtils.from("isInstanced", false));
        IteratorUtils.forEach(context.scene().getMeshSceneElements(), (final Mesh mesh, final List<SceneElement> elements) -> {
            shader.setUniform("material", mesh.getMaterial());
            final Texture text = mesh.getMaterial().getAlbedo();
            if (text != null) {
                shader.setUniform(new GLUniformData("cols", text.getCols()));
                shader.setUniform(new GLUniformData("rows", text.getRows()));
            }
            context.depthShader().bindTextures(GL2.GL_TEXTURE2);

            mesh.renderList(
                    elements,
                    (final SceneElement sceneElement) -> {
                        shader.setUniform(new GLUniformData("selectedNonInstanced", sceneElement.isSelected() ? 1.0f : 0.0f));
                        shader.setUniform(UniformUtils.from("modelNonInstancedMatrix", context.transform().buildModelMatrix(sceneElement)));
                        if (sceneElement instanceof AnimatedSceneElement animatedSceneElement) {
                            shader.setUniform(UniformUtils.from(
                                    "jointsMatrix",
                                    animatedSceneElement.getCurrentFrame().getJointMatrices()
                            ));
                        }
                    }
            );
        });
    }

    public static void renderInstancedMeshes(final Shader shader,
                                             final RenderContext context) {
        shader.setUniform(UniformUtils.from("isInstanced", true));
        IteratorUtils.forEach(context.scene().getInstancedMeshSceneElements(), (final InstancedMesh mesh, final List<SceneElement> elements) -> {
            final Texture texture = mesh.getMaterial().getAlbedo();
            if (texture != null) {
                shader.setUniform(new GLUniformData("cols", texture.getCols()));
                shader.setUniform(new GLUniformData("rows", texture.getRows()));
            }
            shader.setUniform("material", mesh.getMaterial());
            context.filteredElements().clear();
            elements.stream()
                    .filter(SceneElement::isInsideFrustum)
                    .forEach(context.filteredElements()::add);
            context.depthShader().bindTextures(GL2.GL_TEXTURE2);
            mesh.renderListInstanced(context.filteredElements(), context.transform(), context.camera().getViewMatrix());
        });
    }

}
